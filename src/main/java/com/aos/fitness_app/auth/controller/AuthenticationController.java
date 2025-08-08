package com.aos.fitness_app.auth.controller;


import com.aos.fitness_app.auth.dto.*;
import com.aos.fitness_app.auth.entity.PasswordResetToken;
import com.aos.fitness_app.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@CrossOrigin
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;


    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {

        return ResponseEntity.ok(service.register(request));
    }
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }


    @GetMapping("/validate")
    public void validate() {

    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestParam String email) {
        PasswordResetToken resetToken = service.generateForgotPasswordToken(email);
        ForgotPasswordResponse passwordResetResponse = service.generatePasswordResetResponse(resetToken);
        return ResponseEntity.ok(passwordResetResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        Boolean isPasswordReset = service.resetPassword(request);
        if(!isPasswordReset){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expired");
        }
        return ResponseEntity.ok("Password successfully reset");
    }
}
