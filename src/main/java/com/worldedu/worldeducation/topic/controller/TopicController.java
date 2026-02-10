package com.worldedu.worldeducation.topic.controller;

import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.topic.dto.TopicListResponse;
import com.worldedu.worldeducation.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Slf4j
public class TopicController {

    private final TopicService topicService;

    /**
     * Get opted and unopted topics for a subject
     * GET /api/topics/subject/{subjectId}
     * 
     * Requires: JWT authentication
     * Returns: List of opted topics and list of unopted topics
     * 
     * @param subjectId The subject ID to fetch topics for
     * @param user The authenticated user (injected by Spring Security)
     * @return TopicListResponse containing opted and unopted topics
     */
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<ApiResponse<TopicListResponse>> getTopicsBySubject(
            @PathVariable Long subjectId,
            @AuthenticationPrincipal User user) {
        
        log.info("User {} requesting topics for subjectId: {}", user.getUserId(), subjectId);
        
        TopicListResponse response = topicService.getTopicsBySubject(subjectId, user.getCustomerId());
        
        return ResponseEntity.ok(
            ApiResponse.success("Topics retrieved successfully", response)
        );
    }
}
