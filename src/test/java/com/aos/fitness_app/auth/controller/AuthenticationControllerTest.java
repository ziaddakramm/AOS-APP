package com.aos.fitness_app.auth.controller;

import com.aos.fitness_app.auth.dto.*;
import com.aos.fitness_app.auth.enums.Role;
import com.aos.fitness_app.auth.exception.AuthExceptionHandler;
import com.aos.fitness_app.auth.service.ApplicationUserService;
import com.aos.fitness_app.auth.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Controller Tests")
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ApplicationUserService applicationUserService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;
    private VerifyOtpRequest verifyOtpRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private ForgotPasswordRequest forgotPasswordRequest;

    @BeforeEach
    void setUp() {
        // Initialize MockMvc with the controller
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new AuthExceptionHandler()) // Use your actual exception handler
                .build();

        reset(authenticationService, applicationUserService);

        // Initialize ObjectMapper
        objectMapper = new ObjectMapper();

        // Initialize test data objects
        registerRequest = RegisterRequest.builder()
                .email("auth@gmail.com")
                .password("password123")
                .role(Role.USER)
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .email("auth@gmail.com")
                .password("password123")
                .build();

        authenticationResponse = AuthenticationResponse.builder()
                .accessToken("123456")
                .email("auth@gmail.com")
                .expiration(40)
                .build();

        verifyOtpRequest = VerifyOtpRequest.builder()
                .email("auth@gmail.com")
                .otp("123456")
                .build();

        resetPasswordRequest = ResetPasswordRequest.builder()
                .email("auth@gmail.com")
                .otp("123456")
                .newPassword("newPassword123")
                .build();

        forgotPasswordRequest = ForgotPasswordRequest.builder()
                .email("auth@gmail.com")
                .build();
    }




    //Registration
    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        // Given
        given(authenticationService.register(any(RegisterRequest.class)))
                .willReturn(authenticationResponse);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").value("123456"))
                .andExpect(jsonPath("$.email").value("auth@gmail.com"))
                .andExpect(jsonPath("$.expiration").value(40));

        verify(authenticationService, times(1)).register(any(RegisterRequest.class));
    }



    @Test
    @DisplayName("Should handle service exception during registration")
    void shouldHandleServiceExceptionDuringRegistration() throws Exception {
        // Given
        given(authenticationService.register(any(RegisterRequest.class)))
                .willThrow(new RuntimeException("Registration failed"));

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().is5xxServerError());

        verify(authenticationService, times(1)).register(any(RegisterRequest.class));
    }


    //Authentication
    @Test
    @DisplayName("Should authenticate user successfully")
    void shouldAuthenticateUserSuccessfully() throws Exception {
        // Given
        given(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .willReturn(authenticationResponse);

        // When & Then
        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").value("123456"))
                .andExpect(jsonPath("$.email").value("auth@gmail.com"))
                .andExpect(jsonPath("$.expiration").value(40));

        verify(authenticationService, times(1)).authenticate(any(AuthenticationRequest.class));
    }


    @Test
    @DisplayName("Should handle authentication service throwing exception")
    void shouldHandleAuthenticationServiceException() throws Exception {
        // Given
        given(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Bad credentials"))
                .andExpect(jsonPath("$.timeStamp").exists());

        verify(authenticationService, times(1)).authenticate(any(AuthenticationRequest.class));
    }



        //Forgot password
        @Test
        @DisplayName("Should generate and send OTP for forgot password")
        void shouldGenerateAndSendOtpForForgotPassword() throws Exception {
            // Given
            String email = "test@example.com";
            doNothing().when(authenticationService).generateResetOtp(forgotPasswordRequest.getEmail());

            // When & Then
            mockMvc.perform(post("/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("OTP sent to your email. Please check your inbox."));
        }

        @Test
        @DisplayName("Should handle service exception during OTP generation")
        void shouldHandleServiceExceptionDuringOtpGeneration() throws Exception {
            // Given
            doThrow(new RuntimeException("Email service unavailable"))
                    .when(authenticationService).generateResetOtp(forgotPasswordRequest.getEmail());

            // When & Then
            mockMvc.perform(post("/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError()) // Your AuthExceptionHandler returns 400
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Email service unavailable"))
                    .andExpect(jsonPath("$.timeStamp").exists());

            verify(authenticationService, times(1)).generateResetOtp(forgotPasswordRequest.getEmail());
        }



        @Test
        @DisplayName("Should verify OTP successfully when valid")
        void shouldVerifyOtpSuccessfullyWhenValid() throws Exception {
            // Given
            given(authenticationService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp()))
                    .willReturn(true);

            // When & Then
            mockMvc.perform(post("/auth/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.email").value("auth@gmail.com"));

            verify(authenticationService, times(1)).verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
        }

        @Test
        @DisplayName("Should return false when OTP is invalid")
        void shouldReturnFalseWhenOtpIsInvalid() throws Exception {
            // Given
            given(authenticationService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp()))
                    .willReturn(false);
            // When & Then
            mockMvc.perform(post("/auth/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.email").value("auth@gmail.com"));

        }

        @Test
        @DisplayName("Should handle service exception during OTP verification")
        void shouldHandleServiceExceptionDuringOtpVerification() throws Exception {
            // Given
            given(authenticationService.verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp()))
                    .willThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(post("/auth/verify-otp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Database connection failed"))
                    .andExpect(jsonPath("$.timeStamp").exists());

            verify(authenticationService, times(1)).verifyOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());
        }

        @Test
        @DisplayName("Should reset password successfully when OTP is valid")
        void shouldResetPasswordSuccessfullyWhenOtpIsValid() throws Exception {
            // Given
            given(authenticationService.verifyOtp(resetPasswordRequest.getEmail(), resetPasswordRequest.getOtp()))
                    .willReturn(true);
            doNothing().when(applicationUserService)
                    .updatePasswordByEmail(resetPasswordRequest.getEmail(), resetPasswordRequest.getNewPassword());
            doNothing().when(authenticationService)
                    .markOtpAsUsed(resetPasswordRequest.getEmail(), resetPasswordRequest.getOtp());

            // When & Then
            mockMvc.perform(post("/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.message").value("Password reset successfully"));

//            verify(authenticationService, times(1)).verifyOtp("test@example.com", "123456");
//            verify(applicationUserService, times(1))
//                    .updatePasswordByEmail("test@example.com", "newPassword123");
//            verify(authenticationService, times(1))
//                    .markOtpAsUsed("test@example.com", "123456");
        }

        @Test
        @DisplayName("Should fail to reset password when OTP is invalid")
        void shouldFailToResetPasswordWhenOtpIsInvalid() throws Exception {
            // Given
            given(authenticationService.verifyOtp(resetPasswordRequest.getEmail(), resetPasswordRequest.getOtp()))
                    .willReturn(false);

            // When & Then
            mockMvc.perform(post("/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));

            verify(authenticationService, times(1)).verifyOtp(resetPasswordRequest.getEmail(), resetPasswordRequest.getOtp());
            verify(applicationUserService, never()).updatePasswordByEmail(anyString(), anyString());
            verify(authenticationService, never()).markOtpAsUsed(anyString(), anyString());
        }

        @Test
        @DisplayName("Should handle service exceptions during password reset")
        void shouldHandleServiceExceptionsDuringPasswordReset() throws Exception {
            // Given
            given(authenticationService.verifyOtp(resetPasswordRequest.getEmail(), resetPasswordRequest.getOtp()))
                    .willReturn(true);
            doThrow(new RuntimeException("Database error"))
                    .when(applicationUserService)
                    .updatePasswordByEmail(resetPasswordRequest.getEmail(),  resetPasswordRequest.getNewPassword());

            // When & Then
            mockMvc.perform(post("/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                    .andDo(print())
                    .andExpect(status().is5xxServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message").value("Database error"))
                    .andExpect(jsonPath("$.timeStamp").exists());

            verify(authenticationService, times(1)).verifyOtp(resetPasswordRequest.getEmail(),  resetPasswordRequest.getOtp());
            verify(applicationUserService, times(1))
                    .updatePasswordByEmail(resetPasswordRequest.getEmail(),  resetPasswordRequest.getNewPassword());
            verify(authenticationService, never()).markOtpAsUsed(anyString(), anyString());
        }

        @Test
        @DisplayName("Should handle null request body gracefully")
        void shouldHandleNullRequestBodyGracefully() throws Exception {
            // When & Then
            mockMvc.perform(post("/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("null"))
                    .andDo(print())
                    .andExpect(status().is5xxServerError());

            verify(authenticationService, never()).verifyOtp(anyString(), anyString());
            verify(applicationUserService, never()).updatePasswordByEmail(anyString(), anyString());
            verify(authenticationService, never()).markOtpAsUsed(anyString(), anyString());
        }
    }



