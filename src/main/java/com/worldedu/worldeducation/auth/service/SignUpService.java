package com.worldedu.worldeducation.auth.service;

import com.worldedu.worldeducation.auth.dto.*;
import com.worldedu.worldeducation.auth.entity.CodeVerification;
import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.auth.entity.UserProfile;
import com.worldedu.worldeducation.auth.repository.CodeVerificationRepository;
import com.worldedu.worldeducation.auth.repository.UserProfileRepository;
import com.worldedu.worldeducation.auth.repository.UserRepository;
import com.worldedu.worldeducation.auth.util.PasswordUtil;
import com.worldedu.worldeducation.enums.SignUpMethod;
import com.worldedu.worldeducation.enums.UserCategory;
import com.worldedu.worldeducation.enums.VerificationStatus;
import com.worldedu.worldeducation.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for user signup and Google OAuth authentication
 * Handles two-step signup process with email verification
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignUpService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final CodeVerificationRepository codeVerificationRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    
    @Value("${app.verification.code.length:6}")
    private int codeLength;
    
    @Value("${app.verification.code.validity-minutes:15}")
    private int codeValidityMinutes;
    
    // Temporary storage for signup data (in production, use Redis or database)
    private final Map<String, SignUpRequest> pendingSignUps = new HashMap<>();
    
    private final SecureRandom random = new SecureRandom();

    /**
     * Step 1: Initiate signup - Validate data, generate code, send email
     * @param request SignUp request with all user details
     * @return SignUpResponse with success message
     */
    @Transactional
    public SignUpResponse initiateSignUp(SignUpRequest request) {
        log.info("Initiating signup for email: {}", request.getEmail());
        
        // Validate user doesn't already exist
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new RuntimeException("User ID already exists: " + request.getUserId());
        }
        
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }
        
        // Invalidate any previous codes for this email
        invalidatePreviousCodes(request.getEmail());
        
        // Generate verification code
        String verificationCode = generateVerificationCode();
        
        // Store verification code in database
        CodeVerification codeVerification = new CodeVerification();
        codeVerification.setSecretCode(verificationCode);
        codeVerification.setAction("SIGNUP");
        codeVerification.setUserId(request.getEmail());  // Use email as identifier
        codeVerification.setGenerationTime(LocalDateTime.now());
        codeVerification.setExpiryTime(LocalDateTime.now().plusMinutes(codeValidityMinutes));
        codeVerification.setStatus(VerificationStatus.ACTIVE);
        
        codeVerificationRepository.save(codeVerification);
        log.info("Verification code saved for email: {}", request.getEmail());
        
        // Store signup request temporarily (in production, use Redis with TTL)
        pendingSignUps.put(request.getEmail(), request);
        
        // Send verification email
        emailService.sendVerificationCode(request.getEmail(), verificationCode, codeValidityMinutes);
        
        return SignUpResponse.builder()
                .message("Verification code sent to your email")
                .email(request.getEmail())
                .codeValidityMinutes(codeValidityMinutes)
                .build();
    }

    /**
     * Step 2: Verify code and create user account
     * @param request VerifyCodeRequest with email and code
     * @return LoginResponse with JWT token
     */
    @Transactional
    public LoginResponse verifyAndCreateUser(VerifyCodeRequest request) {
        log.info("Verifying code for email: {}", request.getEmail());
        
        // Find active verification code
        Optional<CodeVerification> codeOpt = codeVerificationRepository
                .findByUserIdAndSecretCodeAndStatusAndActionAndExpiryTimeAfter(
                        request.getEmail(),
                        request.getCode(),
                        VerificationStatus.ACTIVE,
                        "SIGNUP",
                        LocalDateTime.now()
                );
        
        if (codeOpt.isEmpty()) {
            throw new RuntimeException("Invalid or expired verification code");
        }
        
        // Get pending signup data
        SignUpRequest signUpRequest = pendingSignUps.get(request.getEmail());
        if (signUpRequest == null) {
            throw new RuntimeException("Signup session expired. Please restart signup process.");
        }
        
        // Mark code as USED
        CodeVerification codeVerification = codeOpt.get();
        codeVerification.setStatus(VerificationStatus.USED);
        codeVerificationRepository.save(codeVerification);
        
        // Create User
        User user = new User();
        user.setUserId(signUpRequest.getUserId());
        user.setPasswordHash(PasswordUtil.hashPassword(signUpRequest.getPassword()));
        user.setUserCategory(signUpRequest.getUserCategory());
        user.setSignUpMethod(SignUpMethod.DATA);
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setPasswordExpiry(LocalDateTime.now().plusMonths(6));  // 6 months validity
        
        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getUserId());
        
        // Create User Profile
        UserProfile profile = new UserProfile();
        profile.setCustomerId(savedUser.getCustomerId());
        profile.setFirstName(signUpRequest.getFirstName());
        profile.setMiddleName(signUpRequest.getMiddleName());
        profile.setLastName(signUpRequest.getLastName());
        profile.setEmail(signUpRequest.getEmail());
        profile.setMobileNo(signUpRequest.getMobileNo());
        profile.setCountry(signUpRequest.getCountry());
        profile.setState(signUpRequest.getState());
        profile.setCity(signUpRequest.getCity());
        profile.setAddress(signUpRequest.getAddress());
        
        userProfileRepository.save(profile);
        log.info("User profile created for customerId: {}", savedUser.getCustomerId());
        
        // Remove from pending signups
        pendingSignUps.remove(request.getEmail());
        
        // Send welcome email
        emailService.sendWelcomeEmail(
                signUpRequest.getEmail(),
                signUpRequest.getFirstName(),
                signUpRequest.getUserId()
        );
        
        // Generate JWT token for automatic login
        String token = jwtUtil.generateToken(
                savedUser.getUserId(),
                savedUser.getCustomerId(),
                savedUser.getUserCategory().name()
        );
        
        return LoginResponse.builder()
                .message("Signup successful! Welcome to World Education.")
                .token(token)
                .userId(savedUser.getUserId())
                .customerId(savedUser.getCustomerId())
                .userCategory(savedUser.getUserCategory())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(profile.getEmail())
                .build();
    }

    /**
     * Google OAuth authentication - handles both signup and signin
     * @param request GoogleAuthRequest with Google token and profile data
     * @return LoginResponse with JWT token
     */
    @Transactional
    public LoginResponse googleAuth(GoogleAuthRequest request) {
        log.info("Google OAuth authentication for email: {}", request.getEmail());
        
        // TODO: Validate Google token with Google API
        // For now, we'll assume the frontend has already validated the token
        // In production, verify the googleToken using Google's token verification API
        
        // Check if user exists by email
        Optional<UserProfile> profileOpt = userProfileRepository.findByEmail(request.getEmail());
        
        if (profileOpt.isPresent()) {
            // User exists - Sign In
            UserProfile profile = profileOpt.get();
            User user = userRepository.findByCustomerId(profile.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify this is a Google user
            if (user.getSignUpMethod() != SignUpMethod.GOOGLE) {
                throw new RuntimeException("This email is registered with password. Please use password login.");
            }
            
            // Update last login
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(
                    user.getUserId(),
                    user.getCustomerId(),
                    user.getUserCategory().name()
            );
            
            log.info("Google Sign-In successful for user: {}", user.getUserId());
            
            return LoginResponse.builder()
                    .message("Google Sign-In successful!")
                    .token(token)
                    .userId(user.getUserId())
                    .customerId(user.getCustomerId())
                    .userCategory(user.getUserCategory())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .email(profile.getEmail())
                    .build();
            
        } else {
            // User doesn't exist - Sign Up
            
            // Validate required fields
            if (request.getEmail() == null || request.getFirstName() == null || request.getLastName() == null) {
                throw new RuntimeException("Email, first name, and last name are required for Google signup");
            }
            
            // Generate unique userId from email
            String userId = generateUserIdFromEmail(request.getEmail());
            
            // Create User (no password for Google users)
            User user = new User();
            user.setUserId(userId);
            user.setPasswordHash(PasswordUtil.hashPassword("GOOGLE_OAUTH_" + request.getGoogleId())); // Dummy password
            user.setUserCategory(UserCategory.STUDENT);  // Default to STUDENT
            user.setSignUpMethod(SignUpMethod.GOOGLE);
            user.setFailedLoginAttempts(0);
            user.setAccountLocked(false);
            user.setPasswordExpiry(null);  // No password expiry for Google users
            
            User savedUser = userRepository.save(user);
            log.info("Google user created: {}", savedUser.getUserId());
            
            // Create User Profile
            UserProfile profile = new UserProfile();
//            profile.setCustomerId(savedUser.getCustomerId());
            profile.setUser(user);
            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setEmail(request.getEmail());
            profile.setMobileNo(request.getMobileNo());
            profile.setCountry(request.getCountry());
            profile.setState(request.getState());
            profile.setCity(request.getCity());
            profile.setAddress(request.getAddress());
            
            userProfileRepository.save(profile);
            log.info("Google user profile created for customerId: {}", savedUser.getCustomerId());
            
            // Send welcome email
            emailService.sendWelcomeEmail(request.getEmail(), request.getFirstName(), userId);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(
                    savedUser.getUserId(),
                    savedUser.getCustomerId(),
                    savedUser.getUserCategory().name()
            );
            
            log.info("Google Sign-Up successful for user: {}", savedUser.getUserId());
            
            return LoginResponse.builder()
                    .message("Google Sign-Up successful! Welcome to World Education.")
                    .token(token)
                    .userId(savedUser.getUserId())
                    .customerId(savedUser.getCustomerId())
                    .userCategory(savedUser.getUserCategory())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .email(profile.getEmail())
                    .build();
        }
    }

    /**
     * Generate random verification code
     */
    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Invalidate all previous active codes for an email
     */
    private void invalidatePreviousCodes(String email) {
        List<CodeVerification> previousCodes = codeVerificationRepository
                .findByUserIdAndAction(email, "SIGNUP");
        
        for (CodeVerification code : previousCodes) {
            if (code.getStatus() == VerificationStatus.ACTIVE) {
                code.setStatus(VerificationStatus.EXPIRED);
            }
        }
        
        if (!previousCodes.isEmpty()) {
            codeVerificationRepository.saveAll(previousCodes);
            log.info("Invalidated {} previous codes for email: {}", previousCodes.size(), email);
        }
    }

    /**
     * Generate unique userId from email
     */
    private String generateUserIdFromEmail(String email) {
        String baseUserId = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        
        // Check if userId already exists
        if (!userRepository.existsByUserId(baseUserId)) {
            return baseUserId;
        }
        
        // Append numbers until unique
        int counter = 1;
        String userId = baseUserId + counter;
        while (userRepository.existsByUserId(userId)) {
            counter++;
            userId = baseUserId + counter;
        }
        
        return userId;
    }
}
