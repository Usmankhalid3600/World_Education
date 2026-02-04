package com.worldedu.worldeducation.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateTimeUtil {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get current date and time
     */
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    /**
     * Format LocalDateTime to string
     */
    public String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * Format LocalDateTime with custom pattern
     */
    public String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Check if a date is expired
     */
    public boolean isExpired(LocalDateTime expiryDate) {
        if (expiryDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Add days to current date
     */
    public LocalDateTime addDays(int days) {
        return LocalDateTime.now().plusDays(days);
    }

    /**
     * Add days to a specific date
     */
    public LocalDateTime addDays(LocalDateTime dateTime, int days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }
}
