package com.worldedu.worldeducation.auth.service;

import com.worldedu.worldeducation.auth.dto.ForgotPasswordResponse;
import com.worldedu.worldeducation.auth.entity.CodeVerification;
import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.auth.entity.UserProfile;
import com.worldedu.worldeducation.auth.repository.CodeVerificationRepository;
import com.worldedu.worldeducation.auth.repository.UserProfileRepository;
import com.worldedu.worldeducation.auth.repository.UserRepository;
import com.worldedu.worldeducation.auth.util.PasswordUtil;
import com.worldedu.worldeducation.enums.VerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Handles the forgot-password / reset-password flow.
 *
 * Flow:
 *  1. initiatePasswordReset(userId)  →  sends 6-digit code to user's email
 *  2. verifyResetCode(userId, code)  →  validates that code is still active (preview step)
 *  3. resetPassword(userId, code, newPassword)  →  re-validates code, updates password, marks code USED
 *
 * Security note: step 1 always returns a "success" response even when the userId is not found,
 * to prevent user-enumeration attacks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final String ACTION = "PASSWORD_RESET";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CodeVerificationRepository codeVerificationRepository;
    private final EmailService emailService;

    @Value("${app.verification.code.length:6}")
    private int codeLength;

    @Value("${app.verification.code.validity-minutes:15}")
    private int codeValidityMinutes;

    private final SecureRandom random = new SecureRandom();

    // ─── Step 1 ───────────────────────────────────────────────────────────────

    /**
     * Initiates a password reset for the given userId.
     * Always returns a success response regardless of whether the user exists,
     * to prevent user-enumeration. An actual email is only sent when the user is found.
     */
    @Transactional
    public ForgotPasswordResponse initiatePasswordReset(String userId) {
        log.info("Password reset requested for userId: {}", userId);

        Optional<User> userOpt = userRepository.findByUserId(userId);

        if (userOpt.isPresent()) {
            Long customerId = userOpt.get().getCustomerId();
            Optional<UserProfile> profileOpt = userProfileRepository.findById(customerId);

            if (profileOpt.isPresent()) {
                String email = profileOpt.get().getEmail();

                // Expire any previous active reset codes for this userId
                expirePreviousCodes(userId);

                // Generate and persist new code
                String code = generateCode();
                CodeVerification cv = new CodeVerification();
                cv.setSecretCode(code);
                cv.setAction(ACTION);
                cv.setUserId(userId);
                cv.setGenerationTime(LocalDateTime.now());
                cv.setExpiryTime(LocalDateTime.now().plusMinutes(codeValidityMinutes));
                cv.setStatus(VerificationStatus.ACTIVE);
                codeVerificationRepository.save(cv);

                // Send the code by email
                emailService.sendPasswordResetCode(email, code, codeValidityMinutes);

                log.info("Reset code sent to masked email for userId: {}", userId);
                return ForgotPasswordResponse.builder()
                        .maskedEmail(maskEmail(email))
                        .codeValidityMinutes(codeValidityMinutes)
                        .build();
            }
        }

        // User not found — return a plausible-looking masked email so the UI stays identical
        log.warn("Password reset requested for unknown userId: {} — proceeding silently", userId);
        return ForgotPasswordResponse.builder()
                .maskedEmail(fakeMaskedEmail(userId))
                .codeValidityMinutes(codeValidityMinutes)
                .build();
    }

    // ─── Step 2 ───────────────────────────────────────────────────────────────

    /**
     * Validates that the code entered by the user is correct and still active.
     * Does NOT mark the code as used — that happens only in resetPassword().
     *
     * @throws RuntimeException if the code is invalid or expired
     */
    public void verifyResetCode(String userId, String code) {
        log.info("Verifying reset code for userId: {}", userId);
        findActiveCode(userId, code); // throws if not found / expired
        log.info("Reset code verified for userId: {}", userId);
    }

    // ─── Step 3 ───────────────────────────────────────────────────────────────

    /**
     * Resets the user's password after re-validating the code.
     * Marks the code as USED and unlocks the account if it was locked.
     *
     * @throws RuntimeException if code is invalid/expired or user is not found
     */
    @Transactional
    public void resetPassword(String userId, String code, String newPassword) {
        log.info("Resetting password for userId: {}", userId);

        CodeVerification cv = findActiveCode(userId, code);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update password and reset lock state
        user.setPasswordHash(PasswordUtil.hashPassword(newPassword));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setPasswordExpiry(LocalDateTime.now().plusMonths(6));
        userRepository.save(user);

        // Mark code as used so it cannot be reused
        cv.setStatus(VerificationStatus.USED);
        codeVerificationRepository.save(cv);

        log.info("Password reset successfully for userId: {}", userId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CodeVerification findActiveCode(String userId, String code) {
        return codeVerificationRepository
                .findByUserIdAndSecretCodeAndStatusAndActionAndExpiryTimeAfter(
                        userId, code, VerificationStatus.ACTIVE, ACTION, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification code"));
    }

    private void expirePreviousCodes(String userId) {
        List<CodeVerification> previous = codeVerificationRepository.findByUserIdAndAction(userId, ACTION);
        previous.stream()
                .filter(c -> c.getStatus() == VerificationStatus.ACTIVE)
                .forEach(c -> c.setStatus(VerificationStatus.EXPIRED));
        if (!previous.isEmpty()) {
            codeVerificationRepository.saveAll(previous);
        }
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Masks a real email: "john.doe@gmail.com" → "j***e@g***.com"
     */
    private String maskEmail(String email) {
        String[] parts = email.split("@");
        if (parts.length != 2) return "****@****.***";

        String local = parts[0];
        String domain = parts[1];

        String maskedLocal;
        if (local.length() <= 2) {
            maskedLocal = local.charAt(0) + "***";
        } else {
            maskedLocal = local.charAt(0)
                    + "*".repeat(Math.min(local.length() - 2, 4))
                    + local.charAt(local.length() - 1);
        }

        String[] domainParts = domain.split("\\.");
        String maskedDomain = domainParts[0].charAt(0)
                + "*".repeat(Math.min(domainParts[0].length() - 1, 3))
                + "." + domainParts[domainParts.length - 1];

        return maskedLocal + "@" + maskedDomain;
    }

    /**
     * Generates a plausible-looking masked email for a non-existent userId
     * so the UI flow stays identical to the real case.
     */
    private String fakeMaskedEmail(String userId) {
        String base = userId.length() > 0 ? String.valueOf(userId.charAt(0)) : "u";
        return base + "***@****.com";
    }
}
