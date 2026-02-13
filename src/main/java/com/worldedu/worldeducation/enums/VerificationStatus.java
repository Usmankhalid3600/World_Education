package com.worldedu.worldeducation.enums;

/**
 * Enum for code verification status
 * ACTIVE - Code is valid and can be used
 * USED - Code has been used successfully
 * EXPIRED - Code has expired
 * LOGGED - Code was logged for audit purposes
 */
public enum VerificationStatus {
    ACTIVE,
    USED,
    EXPIRED,
    LOGGED
}
