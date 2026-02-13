package com.worldedu.worldeducation.auth.controller;

import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.auth.dto.*;
import com.worldedu.worldeducation.auth.service.AuthService;
import com.worldedu.worldeducation.auth.service.SignUpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final SignUpService signUpService;

    /**
     * Login endpoint
     * POST /api/auth/login
     * 
     * Accepts user credentials and device information
     * Returns user information and session details on successful authentication
     * 
     * Features:
     * - Single device login for STUDENT users
     * - Multi-device login allowed for ADMIN users
     * - Account locking after 5 failed attempts
     * - Session management
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUserId());
        
        LoginResponse loginResponse = authService.login(loginRequest);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("Login successful", loginResponse));
    }

    /**
     * SignUp endpoint - Step 1: Initiate signup and send verification code
     * POST /api/auth/signup
     * 
     * Collects user information and sends verification code to email
     * User must verify code in next step to complete signup
     * 
     * Request body:
     * {
     *   "userId": "john_doe",
     *   "password": "SecurePass123",
     *   "userCategory": "STUDENT",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john@example.com",
     *   "mobileNo": "+1234567890",
     *   "country": "USA",
     *   "state": "California",
     *   "city": "Los Angeles"
     * }
     * 
     * @param request SignUpRequest with all user details
     * @return SignUpResponse with verification code sent message
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        log.info("Signup request received for userId: {} and email: {}", request.getUserId(), request.getEmail());
        
        try {
            SignUpResponse response = signUpService.initiateSignUp(request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(response.getMessage(), response));
        } catch (RuntimeException e) {
            log.error("Signup failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Verify code endpoint - Step 2: Verify code and create user
     * POST /api/auth/verify
     * 
     * Verifies the code sent to user's email and creates the account
     * Returns JWT token for automatic login after successful signup
     * 
     * Request body:
     * {
     *   "email": "john@example.com",
     *   "code": "123456"
     * }
     * 
     * @param request VerifyCodeRequest with email and verification code
     * @return LoginResponse with JWT token and user details
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        log.info("Verification request received for email: {}", request.getEmail());
        
        try {
            LoginResponse response = signUpService.verifyAndCreateUser(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response.getMessage(), response));
        } catch (RuntimeException e) {
            log.error("Verification failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Google OAuth authentication endpoint
     * POST /api/auth/google
     * 
     * Handles both Google Sign-In and Sign-Up:
     * - If email exists: Signs in the user (must be a Google user)
     * - If email doesn't exist: Creates new account with signUp_method=GOOGLE
     * 
     * No password required for Google users
     * Returns JWT token for authenticated session
     * 
     * Request body:
     * {
     *   "googleToken": "google_id_token_from_frontend",
     *   "email": "john@gmail.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "googleId": "1234567890",
     *   "mobileNo": "+1234567890",
     *   "country": "USA"
     * }
     * 
     * @param request GoogleAuthRequest with Google token and profile data
     * @return LoginResponse with JWT token and user details
     */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleAuth(@RequestBody GoogleAuthRequest request) {
        log.info("Google OAuth request received for email: {}", request.getEmail());
        
        try {
            LoginResponse response = signUpService.googleAuth(request);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(response.getMessage(), response));
        } catch (RuntimeException e) {
            log.error("Google authentication failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}
