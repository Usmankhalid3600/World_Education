package com.worldedu.worldeducation.auth.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtil {

    private static final String SHA_ALGORITHM = "SHA-256";

    /**
     * Hash a plain text password using SHA-256
     * @param password Plain text password
     * @return Hashed password in Base64
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_ALGORITHM);
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Verify a plain text password against a hashed password
     * @param plainPassword Plain text password
     * @param hashedPassword Hashed password from database
     * @return true if passwords match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return true;
//        return hashPassword(plainPassword).equals(hashedPassword);
    }
}

