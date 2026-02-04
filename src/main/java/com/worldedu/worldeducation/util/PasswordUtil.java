package com.worldedu.worldeducation.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class PasswordUtil {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    /**
     * Hash password using SHA-256
     * Note: In production, use BCryptPasswordEncoder from Spring Security
     */
    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify if password matches the hash
     */
    public boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }

    /**
     * Check if account should be locked based on failed attempts
     */
    public boolean shouldLockAccount(int failedAttempts) {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    /**
     * Get maximum allowed failed attempts
     */
    public int getMaxFailedAttempts() {
        return MAX_FAILED_ATTEMPTS;
    }
}
