package com.worldedu.worldeducation.service;

import com.worldedu.worldeducation.dto.LoginRequest;
import com.worldedu.worldeducation.dto.LoginResponse;
import com.worldedu.worldeducation.entity.User;
import com.worldedu.worldeducation.entity.UserProfile;
import com.worldedu.worldeducation.entity.UserSession;
import com.worldedu.worldeducation.enums.UserCategory;
import com.worldedu.worldeducation.exception.AccountLockedException;
import com.worldedu.worldeducation.exception.InvalidCredentialsException;
import com.worldedu.worldeducation.repository.UserProfileRepository;
import com.worldedu.worldeducation.repository.UserRepository;
import com.worldedu.worldeducation.repository.UserSessionRepository;
import com.worldedu.worldeducation.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordUtil passwordUtil;

    /**
     * Authenticate user and create session
     * Implements single device login for STUDENT users
     * Locks account after 5 failed attempts
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUserId());

        // Find user by userId
        User user = userRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid user ID or password"));

        // Check if account is locked
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            log.warn("Login attempt for locked account: {}", loginRequest.getUserId());
            throw new AccountLockedException(
                    "Account is locked due to multiple failed login attempts. Please contact support.");
        }

        // Update last login attempt
        user.setLastLoginAttemptAt(LocalDateTime.now());

        // Verify password
        if (!passwordUtil.verifyPassword(loginRequest.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid user ID or password");
        }

        // Password is correct - reset failed attempts and proceed with login
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());

        // Handle single device login for STUDENT users
        if (user.getUserCategory() == UserCategory.STUDENT) {
            deactivateExistingSessions(user);
        }

        // Create new session
        UserSession session = createUserSession(user, loginRequest);

        // Save user with updated login info
        userRepository.save(user);

        // Build and return response
        return buildLoginResponse(user, session);
    }

    /**
     * Handle failed login attempt
     * Increments failed attempts counter and locks account if threshold reached
     */
    private void handleFailedLogin(User user) {
        int failedAttempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(failedAttempts);

        if (passwordUtil.shouldLockAccount(failedAttempts)) {
            user.setAccountLocked(true);
            userRepository.save(user);
            log.warn("Account locked for user: {} after {} failed attempts", 
                    user.getUserId(), failedAttempts);
            throw new AccountLockedException(
                    "Account has been locked due to multiple failed login attempts. Please contact support.");
        }

        userRepository.save(user);
        log.warn("Failed login attempt {} of {} for user: {}", 
                failedAttempts, passwordUtil.getMaxFailedAttempts(), user.getUserId());
    }

    /**
     * Deactivate all existing active sessions for the user
     * Used for single device login enforcement
     */
    private void deactivateExistingSessions(User user) {
        log.info("Deactivating existing sessions for STUDENT user: {}", user.getUserId());
        userSessionRepository.deactivateAllSessionsForUser(user);
    }

    /**
     * Create new user session
     */
    private UserSession createUserSession(User user, LoginRequest loginRequest) {
        UserSession session = new UserSession();
        session.setUser(user);
        session.setDeviceId(loginRequest.getDeviceId());
        session.setDeviceType(loginRequest.getDeviceType());
        session.setLoginTime(LocalDateTime.now());
        session.setLastActivityAt(LocalDateTime.now());
        session.setIsActive(true);

        session = userSessionRepository.save(session);
        log.info("Created new session {} for user: {} on device: {}", 
                session.getSessionId(), user.getUserId(), loginRequest.getDeviceType());

        return session;
    }

    /**
     * Build login response with user and session information
     */
    private LoginResponse buildLoginResponse(User user, UserSession session) {
        // Fetch user profile for additional info
        UserProfile userProfile = userProfileRepository.findById(user.getCustomerId())
                .orElse(null);

        LoginResponse.LoginResponseBuilder responseBuilder = LoginResponse.builder()
                .customerId(user.getCustomerId())
                .userId(user.getUserId())
                .userCategory(user.getUserCategory())
                .sessionId(session.getSessionId())
                .loginTime(session.getLoginTime())
                .message("Login successful");

        if (userProfile != null) {
            responseBuilder
                    .firstName(userProfile.getFirstName())
                    .lastName(userProfile.getLastName())
                    .email(userProfile.getEmail());
        }

        log.info("Login successful for user: {}", user.getUserId());
        return responseBuilder.build();
    }
}
