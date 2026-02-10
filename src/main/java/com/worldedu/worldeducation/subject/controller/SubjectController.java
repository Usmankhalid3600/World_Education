package com.worldedu.worldeducation.subject.controller;

import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.subject.dto.SubjectListResponse;
import com.worldedu.worldeducation.subject.service.SubjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * Get opted and unopted subjects for a class
     * GET /api/subjects/class/{classId}
     * 
     * Requires: JWT authentication
     * Returns: List of opted subjects and list of unopted subjects
     * 
     * @param classId The class ID to fetch subjects for
     * @param user The authenticated user (injected by Spring Security)
     * @return SubjectListResponse containing opted and unopted subjects
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<ApiResponse<SubjectListResponse>> getSubjectsByClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal User user) {
        
        log.info("User {} requesting subjects for classId: {}", user.getUserId(), classId);
        
        SubjectListResponse response = subjectService.getSubjectsByClass(classId, user.getCustomerId());
        
        return ResponseEntity.ok(
            ApiResponse.success("Subjects retrieved successfully", response)
        );
    }
}
