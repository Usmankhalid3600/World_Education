package com.worldedu.worldeducation.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern MOBILE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate mobile number format
     */
    public boolean isValidMobile(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile).matches();
    }

    /**
     * Validate password strength
     * At least 8 characters, contains letter and number
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        return hasLetter && hasDigit;
    }

    /**
     * Check if string is null or empty
     */
    public boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
