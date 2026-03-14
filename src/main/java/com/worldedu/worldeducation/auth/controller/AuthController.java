package com.worldedu.worldeducation.auth.controller;

import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.auth.dto.*;
import com.worldedu.worldeducation.auth.service.AuthService;
import com.worldedu.worldeducation.auth.service.PasswordResetService;
import com.worldedu.worldeducation.auth.service.SignUpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final PasswordResetService passwordResetService;

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
     * Session check endpoint — used by the frontend to poll session validity.
     * GET /api/auth/session/check
     *
     * The JWT filter short-circuits with SESSION_TERMINATED (401) before this
     * method is reached when the session has been killed by a new login.
     * Returning 200 here simply confirms the session is still alive.
     */
    @GetMapping("/session/check")
    public ResponseEntity<ApiResponse<Void>> checkSession() {
        return ResponseEntity.ok(ApiResponse.success("Session is active", null));
    }

    /**
     * Logout endpoint — deactivates the current session in the DB.
     * POST /api/auth/logout
     *
     * Marks the session as inactive so any remaining copies of the JWT
     * are rejected by the filter on subsequent requests.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authService.logoutByToken(authHeader.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
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

    /**
     * Forgot Password - Step 1: Send reset code to user's email
     * POST /api/auth/forgot-password
     *
     * Always returns 200 regardless of whether the userId exists (prevents user enumeration).
     * The email is only sent when the user is found.
     *
     * Request body: { "userId": "john_doe" }
     * Response:     { "maskedEmail": "j***n@g***.com", "codeValidityMinutes": 15 }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("Forgot-password request for userId: {}", request.getUserId());
        try {
            ForgotPasswordResponse response = passwordResetService.initiatePasswordReset(request.getUserId());
            return ResponseEntity.ok(
                    ApiResponse.success("If this User ID exists, a reset code has been sent to the associated email.", response));
        } catch (RuntimeException e) {
            log.error("Forgot-password error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to process request. Please try again later.", null));
        }
    }

    /**
     * Forgot Password - Step 2: Verify the reset code
     * POST /api/auth/verify-reset-code
     *
     * Request body: { "userId": "john_doe", "code": "123456" }
     */
    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<Void>> verifyResetCode(
            @Valid @RequestBody VerifyResetCodeRequest request) {

        log.info("Verify-reset-code request for userId: {}", request.getUserId());
        try {
            passwordResetService.verifyResetCode(request.getUserId(), request.getCode());
            return ResponseEntity.ok(ApiResponse.success("Code verified successfully.", null));
        } catch (RuntimeException e) {
            log.error("Verify-reset-code failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }

    /**
     * Forgot Password - Step 3: Set new password
     * POST /api/auth/reset-password
     *
     * Re-validates the code and updates the password in a single atomic operation.
     *
     * Request body: { "userId": "john_doe", "code": "123456", "newPassword": "NewPass@123" }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("Reset-password request for userId: {}", request.getUserId());
        try {
            passwordResetService.resetPassword(request.getUserId(), request.getCode(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please log in with your new password.", null));
        } catch (RuntimeException e) {
            log.error("Reset-password failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage(), null));
        }
    }
}
