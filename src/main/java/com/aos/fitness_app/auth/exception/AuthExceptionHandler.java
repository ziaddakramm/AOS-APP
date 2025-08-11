package com.aos.fitness_app.auth.exception;


import com.aos.fitness_app.auth.dto.AuthErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
public ResponseEntity<AuthErrorResponse> handleAuthenticationException(AuthenticationException exc) {
    AuthErrorResponse errorResponse = new AuthErrorResponse();
    errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
    errorResponse.setMessage("Invalid credentials");
    errorResponse.setTimeStamp(System.currentTimeMillis());
    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
}

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthErrorResponse> handleBadCredentialsException(BadCredentialsException exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setMessage(exc.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<AuthErrorResponse> handleUserNotFoundException(NoSuchElementException exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setMessage("User not found");
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthErrorResponse> handleIllegalArgumentException(IllegalArgumentException exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(exc.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthErrorResponse> handleGenericException(Exception exc) {
        AuthErrorResponse errorResponse = new AuthErrorResponse();
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setMessage(exc.getMessage());
        errorResponse.setTimeStamp(System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
