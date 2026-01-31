package com.aos.fitness_app.auth.controller;


import com.aos.fitness_app.auth.dto.*;
import com.aos.fitness_app.auth.service.ApplicationUserService;
import com.aos.fitness_app.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@CrossOrigin
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final ApplicationUserService applicationUserService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
           @Valid @RequestBody RegisterRequest request
    ) {
        authenticationService.validateEmail(request.getEmail());
        return ResponseEntity.ok(authenticationService.register(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        authenticationService.validateEmail(request.getEmail());
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    //Takes an email as a param
    //Generates an OTP
    //Sends the otp back in an email
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
            authenticationService.validateEmail(forgotPasswordRequest.getEmail());
            authenticationService.generateResetOtp(forgotPasswordRequest.getEmail());
            return ResponseEntity.ok("OTP sent to your email. Please check your inbox.");
    }

    //TODO: check otp validity
    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authenticationService.validateEmail(request.getEmail());
        boolean isValid = authenticationService.verifyOtp(request.getEmail(), request.getOtp());
            if (isValid) {
                log.info("The reset token sent for email: {} is valid",request.getEmail());
                return ResponseEntity.ok(VerifyOtpResponse.builder()
                        .isSuccess(true)
                        .email(request.getEmail())
                        .build());
            } else {
                log.info("The reset token sent for email: {} is invalid",request.getEmail());
                return ResponseEntity.ok(VerifyOtpResponse.builder()
                        .isSuccess(false)
                        .email(request.getEmail())
                        .build());
            }
    }

    // ResetPassword request
    // contains new password
    // update password in db
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
            // Verify OTP again for security
        authenticationService.validateEmail(request.getEmail());
        boolean isValid = authenticationService.verifyOtp(request.getEmail(), request.getOtp());

            if (!isValid) {
                return ResponseEntity.ok(
                        ResetPasswordResponse.builder()
                                .isSuccess(false)
                                .message("Invalid or expired OTP")
                                .build()
                );
            }

            // Update password
            applicationUserService.updatePasswordByEmail(request.getEmail(), request.getNewPassword());

            // Mark OTP as used
            authenticationService.markOtpAsUsed(request.getEmail(), request.getOtp());

            return ResponseEntity.ok(ResetPasswordResponse.builder()
                        .isSuccess(true)
                        .message("Password reset successfully")
                        .build());
    }
}