package com.aos.fitness_app.auth.constraints;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.passay.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword arg0) {
        // Initialization logic if needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }


        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                // Length rule: 8-30 characters
                new LengthRule(8, 30),

                // Requires at least 1 uppercase letter (A-Z)
                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                // Requires at least 1 lowercase letter (a-z)
                new CharacterRule(EnglishCharacterData.LowerCase, 1),

                // Requires at least 1 digit (0-9)
                new CharacterRule(EnglishCharacterData.Digit, 1),

                // Requires at least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
                new CharacterRule(EnglishCharacterData.Special, 1),

                // No white spaces
                new WhitespaceRule()));

        RuleResult result = validator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;
        }


        context.disableDefaultConstraintViolation(); // Add this line!

        // Convert validation messages to a single string using Java Streams
        String messages = validator.getMessages(result)
                .stream()
                .collect(Collectors.joining(", "));

        context.buildConstraintViolationWithTemplate(messages)
                .addConstraintViolation();

        return false;
    }
}