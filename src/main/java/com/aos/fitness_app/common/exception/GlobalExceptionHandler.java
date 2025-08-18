package com.aos.fitness_app.common.exception;


import com.aos.fitness_app.auth.dto.AuthErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;

import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @Hidden
    @org.springframework.web.bind.annotation.ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthenticationException(AuthenticationException exc) {
    AuthErrorResponse errorResponse = new AuthErrorResponse();
    errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
    errorResponse.setMessage("Invalid credentials");
    errorResponse.setTimeStamp(System.currentTimeMillis());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
}

    @Hidden
    @org.springframework.web.bind.annotation.ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthErrorResponse> handleBadCredentialsException(BadCredentialsException exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setMessage(exc.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @Hidden
    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<AuthErrorResponse> handleUserNotFoundException(NoSuchElementException exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("User not found");
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @Hidden
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthErrorResponse> handleIllegalArgumentException(IllegalArgumentException exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(exc.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Hidden
    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<AuthErrorResponse> handleGenericException(Exception exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setMessage(exc.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
