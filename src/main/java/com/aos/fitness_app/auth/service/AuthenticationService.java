package com.aos.fitness_app.auth.service;

import com.aos.fitness_app.common.Constants;
import com.aos.fitness_app.common.Utilities;
import com.aos.fitness_app.auth.component.JwtService;
import com.aos.fitness_app.auth.dto.AuthenticationRequest;
import com.aos.fitness_app.auth.dto.AuthenticationResponse;
import com.aos.fitness_app.auth.dto.RegisterRequest;
import com.aos.fitness_app.auth.entity.ApplicationUser;
import com.aos.fitness_app.auth.entity.PasswordResetOtp;
import com.aos.fitness_app.auth.repository.ApplicationUserRepository;
import com.aos.fitness_app.auth.repository.PasswordResetOtpRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {
  private final ApplicationUserRepository repository;

  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final ApplicationUserRepository applicationUserRepository;
  private final PasswordResetOtpRepository passwordResetOtpRepository;
  private final EmailService emailService;
  private final ApplicationUserService applicationUserService;

  String regexPattern = Constants.EMAIL_REGEX;


  public AuthenticationResponse register(RegisterRequest request) {

    if (emailAlreadyExists(request.getEmail())) {
      throw new RuntimeException("email already exists");
    }

    ApplicationUser user = applicationUserService.createNewUser(request);
    ApplicationUser savedUser = repository.save(user);
    var jwtToken = jwtService.generateToken(savedUser);

    return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .email(savedUser.getEmail())
            .expiration(jwtService.getJwtExpiration())
            .build();
  }


  public AuthenticationResponse authenticate(AuthenticationRequest request) {

      // This will throw BadCredentialsException if credentials are invalid
      authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
      );

    // Find user - this throws NoSuchElementException if user not found
      var user = repository.findByEmail(request.getEmail())
            .orElseThrow(() -> new NoSuchElementException("User not found with email: " + request.getEmail()));

    try {
      var jwtToken = jwtService.generateToken(user);
      return AuthenticationResponse.builder()
              .accessToken(jwtToken)
              .email(user.getEmail())
              .expiration(jwtService.getJwtExpiration())
              .build();
    } catch (Exception e) {
      throw new RuntimeException("Failed to generate authentication token", e);
    }
  }


  public boolean emailAlreadyExists(String email) {
    Optional<ApplicationUser> user = repository.findByEmail(email);
    if (user.isPresent()) {
      return true;
    }
    return false;
  }

  public void generateResetOtp(String email) {

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

  @Scheduled(fixedRate = 300000)
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


    public void validateEmail(String email) {
    boolean isValid = Utilities.patternMatches(email, regexPattern);
      if (!isValid)
      {
        throw new BadCredentialsException("The provided email is not in a valid format");
      }
    }
}