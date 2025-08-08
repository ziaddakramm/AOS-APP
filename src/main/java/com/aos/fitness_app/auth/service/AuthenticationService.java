package com.aos.fitness_app.auth.service;

import com.aos.fitness_app.auth.component.JwtService;
import com.aos.fitness_app.auth.dto.*;
import com.aos.fitness_app.auth.enums.Role;
import com.aos.fitness_app.auth.entity.ApplicationUser;
import com.aos.fitness_app.auth.entity.PasswordResetToken;
import com.aos.fitness_app.auth.repository.ApplicationUserRepository;
import com.aos.fitness_app.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final ApplicationUserRepository repository;

  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final ApplicationUserRepository applicationUserRepository;

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

  public PasswordResetToken generateForgotPasswordToken(String email) {

    ApplicationUser user = applicationUserRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    String token = UUID.randomUUID().toString();

    PasswordResetToken resetToken = PasswordResetToken.builder()
            .user(user)
            .token(token)
            .expiryDate(LocalDateTime.now().plusMinutes(15))
            .build();

    passwordResetTokenRepository.save(resetToken);

    return resetToken;
  }

  public ForgotPasswordResponse generatePasswordResetResponse(PasswordResetToken resetToken){
    return ForgotPasswordResponse.builder()
            .id(resetToken.getId())
            .token(resetToken.getToken())
            .build();
  }

  public Boolean resetPassword(ResetPasswordRequest request)
  {
    String token = request.getForgotPasswordToken();
    String newPassword = request.getNewPassword();



    PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

    if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      return false;
    }

    ApplicationUser user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(newPassword));
    applicationUserRepository.save(user);

    passwordResetTokenRepository.delete(resetToken);
    return  true;
  }
}

