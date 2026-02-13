package com.worldedu.worldeducation.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails
 * Handles verification codes, password resets, notifications, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.name:World Education}")
    private String appName;

    /**
     * Send verification code email
     * @param toEmail Recipient email address
     * @param verificationCode The 6-digit verification code
     * @param validityMinutes How long the code is valid
     */
    public void sendVerificationCode(String toEmail, String verificationCode, int validityMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(appName + " - Email Verification Code");
            
            String emailBody = String.format(
                "Welcome to %s!\n\n" +
                "Your verification code is: %s\n\n" +
                "This code will expire in %d minutes.\n\n" +
                "If you didn't request this code, please ignore this email.\n\n" +
                "Best regards,\n" +
                "%s Team",
                appName, verificationCode, validityMinutes, appName
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Verification code sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send verification code to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email. Please try again later.");
        }
    }

    /**
     * Send welcome email after successful signup
     * @param toEmail Recipient email address
     * @param firstName User's first name
     * @param userId User's login ID
     */
    public void sendWelcomeEmail(String toEmail, String firstName, String userId) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to " + appName + "!");
            
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Welcome to %s! Your account has been created successfully.\n\n" +
                "Your User ID: %s\n\n" +
                "You can now log in and start exploring our educational content.\n\n" +
                        "Best regards,\n" +
                        "%s Team",
                firstName, appName, userId, appName
            );
            
            message.setText(emailBody);
            
            mailSender.send(message);
            log.info("Welcome email sent successfully to: {}", toEmail);
            
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
            // Don't throw exception for welcome email - account already created
        }
    }
}
