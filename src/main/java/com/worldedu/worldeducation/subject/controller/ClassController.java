package com.worldedu.worldeducation.subject.controller;

import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.subject.dto.ClassListResponse;
import com.worldedu.worldeducation.subject.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
public class ClassController {

    private final ClassService classService;

    /**
     * Get all available classes
     * GET /api/classes
     * 
     * Requires: JWT authentication
     * Returns: List of all active classes
     * 
     * @param user The authenticated user (injected by Spring Security)
     * @return ClassListResponse containing all classes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ClassListResponse>> getAllClasses(
            @AuthenticationPrincipal User user) {
        
        log.info("User {} requesting all classes", user.getUserId());
        
        ClassListResponse response = classService.getAllClasses();
        
        return ResponseEntity.ok(
            ApiResponse.success("Classes retrieved successfully", response)
        );
    }
}
