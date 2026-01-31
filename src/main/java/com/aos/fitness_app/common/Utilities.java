package com.aos.fitness_app.common;

import java.util.regex.Pattern;

public final class Utilities {

    private Utilities() {
    }

    public static boolean patternMatches(String emailAddress, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(emailAddress)
                .matches();
    }
}