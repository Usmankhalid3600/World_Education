package com.worldedu.worldeducation.topic.controller;

import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.topic.dto.TopicContentListResponse;
import com.worldedu.worldeducation.topic.dto.TopicContentUploadRequest;
import com.worldedu.worldeducation.topic.dto.TopicListResponse;
import com.worldedu.worldeducation.topic.entity.TopicContent;
import com.worldedu.worldeducation.topic.service.TopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    /**
     * Get topic contents for a specific topic
     * GET /api/topics/{topicId}/contents
     * 
     * Requires: JWT authentication
     * Access: User must have subscription to either the topic OR the parent subject
     * Returns: List of all topic contents if user has access
     * 
     * @param topicId The topic ID to fetch contents for
     * @param user The authenticated user (injected by Spring Security)
     * @return TopicContentListResponse containing all contents and access information
     */
    @GetMapping("/{topicId}/contents")
    public ResponseEntity<ApiResponse<TopicContentListResponse>> getTopicContents(
            @PathVariable Long topicId,
            @AuthenticationPrincipal User user) {
        
        log.info("User {} requesting contents for topicId: {}", user.getUserId(), topicId);
        
        TopicContentListResponse response = topicService.getTopicContents(topicId, user.getCustomerId());
        
        if (!response.getHasAccess()) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("Access denied. Please subscribe to this topic or its parent subject to view contents.", response)
            );
        }
        
        return ResponseEntity.ok(
            ApiResponse.success("Topic contents retrieved successfully", response)
        );
    }

    /**
     * Upload topic content with file data (Multipart form data)
     * POST /api/topics/{topicId}/contents/upload
     * 
     * Requires: JWT authentication
     * Content-Type: multipart/form-data
     * 
     * @param topicId The topic ID to upload content for
     * @param file The file to upload (MultipartFile)
     * @param user The authenticated user (injected by Spring Security)
     * @return Success response with uploaded content details
     */
    @PostMapping("/{topicId}/contents/upload")
    public ResponseEntity<ApiResponse<String>> uploadTopicContent(
            @PathVariable Long topicId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        
        log.info("User {} uploading content for topicId: {}, fileName: {}", 
                user.getUserId(), topicId, file.getOriginalFilename());
        
        try {
            TopicContent saved = topicService.uploadTopicContent(topicId, file, user.getCustomerId());
            
            String message = String.format("File '%s' uploaded successfully. Content ID: %d, Size: %d bytes",
                    saved.getFileName(), saved.getContentId(), saved.getTopicContentData().length);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(message, saved.getContentId().toString())
            );
        } catch (IOException e) {
            log.error("Error uploading file for topicId: {}", topicId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Failed to upload file: " + e.getMessage(), null)
            );
        } catch (RuntimeException e) {
            log.error("Error uploading content: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    /**
     * Upload topic content with Base64 encoded data (JSON request)
     * POST /api/topics/{topicId}/contents/upload-base64
     * 
     * Requires: JWT authentication
     * Content-Type: application/json
     * 
     * Request body:
     * {
     *   "fileName": "document.pdf",
     *   "fileType": "PDF",
     *   "fileDataBase64": "JVBERi0xLjQKJ..."
     * }
     * 
     * @param topicId The topic ID to upload content for
     * @param request The upload request with Base64 data
     * @param user The authenticated user (injected by Spring Security)
     * @return Success response with uploaded content details
     */
    @PostMapping("/{topicId}/contents/upload-base64")
    public ResponseEntity<ApiResponse<String>> uploadTopicContentBase64(
            @PathVariable Long topicId,
            @RequestBody TopicContentUploadRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("User {} uploading Base64 content for topicId: {}, fileName: {}", 
                user.getUserId(), topicId, request.getFileName());
        
        try {
            TopicContent saved = topicService.uploadTopicContentBase64(
                    topicId, 
                    request.getFileName(), 
                    request.getFileType(),
                    request.getFileDataBase64(),
                    user.getCustomerId()
            );
            
            String message = String.format("File '%s' uploaded successfully. Content ID: %d, Size: %d bytes",
                    saved.getFileName(), saved.getContentId(), saved.getTopicContentData().length);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(message, saved.getContentId().toString())
            );
        } catch (RuntimeException e) {
            log.error("Error uploading Base64 content: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error(e.getMessage(), null)
            );
        }
    }

    /**
     * Delete topic content (soft delete)
     * DELETE /api/topics/contents/{contentId}
     * 
     * Requires: JWT authentication
     * 
     * @param contentId The content ID to delete
     * @param user The authenticated user (injected by Spring Security)
     * @return Success response
     */
    @DeleteMapping("/contents/{contentId}")
    public ResponseEntity<ApiResponse<Void>> deleteTopicContent(
            @PathVariable Long contentId,
            @AuthenticationPrincipal User user) {
        
        log.info("User {} deleting contentId: {}", user.getUserId(), contentId);
        
        try {
            topicService.deleteTopicContent(contentId, user.getCustomerId());
            return ResponseEntity.ok(
                ApiResponse.success("Content deleted successfully", null)
            );
        } catch (RuntimeException e) {
            log.error("Error deleting content: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(e.getMessage(), null)
            );
        }
    }
}
