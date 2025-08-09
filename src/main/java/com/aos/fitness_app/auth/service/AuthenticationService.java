package com.aos.fitness_app.auth.service;

import com.aos.fitness_app.auth.component.JwtService;
import com.aos.fitness_app.auth.dto.AuthenticationRequest;
import com.aos.fitness_app.auth.dto.AuthenticationResponse;
import com.aos.fitness_app.auth.dto.RegisterRequest;
import com.aos.fitness_app.auth.entity.ApplicationUser;
import com.aos.fitness_app.auth.entity.PasswordResetOtp;
import com.aos.fitness_app.auth.repository.ApplicationUserRepository;
import com.aos.fitness_app.auth.repository.PasswordResetOtpRepository;
import com.aos.fitness_app.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
  private final ApplicationUserRepository repository;

  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final ApplicationUserRepository applicationUserRepository;
  private final JavaMailSender mailSender;
  private final PasswordResetOtpRepository passwordResetOtpRepository;
  private final EmailService emailService;

  public AuthenticationResponse register(RegisterRequest request) {

    if (emailAlreadyExists(request.getEmail())) {
      throw new RuntimeException("email already exists");
    }


    var user = ApplicationUser.builder()
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .build();


    repository.save(user);

    var jwtToken = jwtService.generateToken(user);

    return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .email(user.getEmail())
            .expiration(jwtService.getJwtExpiration())
            .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
            )
    );
    var user = repository.findByEmail(request.getEmail())
            .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .email(user.getEmail())
            .expiration(jwtService.getJwtExpiration())
            .build();
  }


  public boolean emailAlreadyExists(String email) {
    Optional<ApplicationUser> user = repository.findByEmail(email);
    if (user.isPresent()) {
      return true;
    }
    return false;
  }

  public PasswordResetOtp generateResetOtp(String email) {

    log.info("Entering generate otp method");
    ApplicationUser user = applicationUserRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));


    // Delete any existing OTP for this email
    passwordResetOtpRepository.deleteByEmail(email);

    // Generate 6-digit OTP
    String otp = generateOtp();

    log.info("Generated otp is {}", otp);

    // Save OTP (expires in 10 minutes)
    PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder()
            .email(email)
            .otp(otp)
            .expiryTime(LocalDateTime.now().plusMinutes(10))
            .build();

    log.info("Password reset otp entity: {}", passwordResetOtp);
    passwordResetOtpRepository.save(passwordResetOtp);
    // TODO: Send OTP via email
    emailService.sendOtpEmail(email, otp);

    return passwordResetOtp;
  }

  private String generateOtp() {
    return String.format("%06d", new Random().nextInt(999999));
  }


  public boolean verifyOtp(String email, String otp) {

    //Find if the otp send is valid
    Optional<PasswordResetOtp> otpEntityOpt = passwordResetOtpRepository.findByEmailAndUsedFalse(email);

    if (otpEntityOpt.isEmpty()) {
      throw new RuntimeException("No valid OTP found for this email: {}" + email);
    }

    PasswordResetOtp otpEntity = otpEntityOpt.get();

    // Check if expired
    if (otpEntity.isExpired()) {
      passwordResetOtpRepository.delete(otpEntity);
      throw new RuntimeException("OTP has expired. Please request a new one");
    }

    // Check max attempts
    if (otpEntity.isMaxAttemptsReached()) {
      passwordResetOtpRepository.delete(otpEntity);
      throw new RuntimeException("Maximum OTP verification attempts reached. Please request a new OTP");
    }

    // Increment attempts
    otpEntity.setAttempts(otpEntity.getAttempts() + 1);
    passwordResetOtpRepository.save(otpEntity);

    // Verify OTP
    if (!otpEntity.getOtp().equals(otp)) {
      throw new RuntimeException("Invalid OTP. " + (3 - otpEntity.getAttempts()) + " attempts remaining");
    }

    return true;
  }

  public void markOtpAsUsed(String email, String otp) {
    PasswordResetOtp otpEntity = passwordResetOtpRepository.findByEmailAndOtpAndUsedFalse(email, otp)
            .orElseThrow(() -> new RuntimeException("OTP not found"));

    otpEntity.setUsed(true);
    passwordResetOtpRepository.save(otpEntity);
  }

  @Scheduled(fixedRate = 60000) // Run every 5 minutes
  public void cleanupExpiredOtps() {
    log.info("Entering expired OTPs cleanup method");
    List<PasswordResetOtp> expiredOtps = passwordResetOtpRepository.findExpiredOtps(LocalDateTime.now());
    if(expiredOtps.isEmpty())
    {
      log.info("No expired OTPs to be deleted");
    }
    else{
      log.info("Deleting expired OTPs");
      passwordResetOtpRepository.deleteAll(expiredOtps);
      log.info("Deleted expired OTPs successfully");
    }
  }

  ////////////////////////////////////////////////

//
//  public PasswordResetToken generateResetToken(String email) {
//
//    ApplicationUser user = applicationUserRepository.findByEmail(email)
//            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//    String token = UUID.randomUUID().toString();
//
//    PasswordResetToken resetToken = PasswordResetToken.builder()
//            .user(user)
//            .token(token)
//            .expiryDate(LocalDateTime.now().plusMinutes(15))
//            .build();
//
//    passwordResetTokenRepository.save(resetToken);
//
//    return resetToken;
//  }
//
//  public ForgotPasswordResponse generatePasswordResetResponse(PasswordResetToken resetToken){
//    return ForgotPasswordResponse.builder()
//            .id(resetToken.getId())
//            .token(resetToken.getToken())
//            .build();
//  }
//
//  public Boolean resetPassword(ResetPasswordRequest request)
//  {
//    String token = request.getForgotPasswordToken();
//    String newPassword = request.getNewPassword();
//
//    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
//            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
//
//    if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
//      return false;
//    }
//
//    ApplicationUser user = resetToken.getUser();
//    user.setPassword(passwordEncoder.encode(newPassword));
//    applicationUserRepository.save(user);
//
//    passwordResetTokenRepository.delete(resetToken);
//    return  true;
//  }

}

