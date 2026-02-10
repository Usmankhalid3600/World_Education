package com.worldedu.worldeducation.auth.controller;

import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.auth.dto.LoginRequest;
import com.worldedu.worldeducation.auth.dto.LoginResponse;
import com.worldedu.worldeducation.auth.service.AuthService;
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
}
