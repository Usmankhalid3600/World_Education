package com.worldedu.worldeducation.common;

public class Constants {

    // Authentication
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    public static final String ACCOUNT_LOCKED_MESSAGE = "Account is locked due to multiple failed login attempts. Please contact support.";
    public static final String INVALID_CREDENTIALS_MESSAGE = "Invalid user ID or password";
    public static final String LOGIN_SUCCESS_MESSAGE = "Login successful";

    // Session
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    // Password
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int PASSWORD_EXPIRY_DAYS = 90;

    // Response Messages
    public static final String OPERATION_SUCCESS = "Operation completed successfully";
    public static final String OPERATION_FAILED = "Operation failed";
    public static final String VALIDATION_ERROR = "Validation error";
    public static final String INTERNAL_ERROR = "Internal server error";

    // Date Format
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_ONLY_FORMAT = "yyyy-MM-dd";
    public static final String TIME_ONLY_FORMAT = "HH:mm:ss";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}
