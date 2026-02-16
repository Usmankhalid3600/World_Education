package com.worldedu.worldeducation.admin.controller;

import com.worldedu.worldeducation.admin.dto.*;
import com.worldedu.worldeducation.admin.service.AdminService;
import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.common.ApiResponse;
import com.worldedu.worldeducation.subject.dto.ClassDTO;
import com.worldedu.worldeducation.subject.dto.SubjectDTO;
import com.worldedu.worldeducation.subscription.dto.CreateSubscriptionPlanRequest;
import com.worldedu.worldeducation.subscription.dto.SubscriptionPlanDTO;
import com.worldedu.worldeducation.subscription.dto.UserSubscriptionDTO;
import com.worldedu.worldeducation.topic.dto.TopicDTO;
import com.worldedu.worldeducation.topic.dto.TopicContentDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    // ============ Class Management ============
    
    @PostMapping("/classes")
    public ResponseEntity<ApiResponse<ClassDTO>> createClass(
            @Valid @RequestBody CreateClassRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} creating new class: {}", user.getUserId(), request.getClassName());
        
        ClassDTO created = adminService.createClass(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Class created successfully", created)
        );
    }

    @PutMapping("/classes/{classId}")
    public ResponseEntity<ApiResponse<ClassDTO>> updateClass(
            @PathVariable Long classId,
            @Valid @RequestBody CreateClassRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} updating class: {}", user.getUserId(), classId);
        
        ClassDTO updated = adminService.updateClass(classId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Class updated successfully", updated)
        );
    }

    @DeleteMapping("/classes/{classId}")
    public ResponseEntity<ApiResponse<Void>> deleteClass(
            @PathVariable Long classId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} deleting class: {}", user.getUserId(), classId);
        
        adminService.deleteClass(classId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Class deleted successfully", null)
        );
    }

    // ============ Subject Management ============
    
    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectDTO>>> getAllSubjects(
            @RequestParam(required = false) Long classId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} fetching all subjects. Class filter: {}", user.getUserId(), classId);
        
        List<SubjectDTO> subjects = adminService.getAllSubjects(classId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Subjects retrieved successfully", subjects)
        );
    }
    
    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectDTO>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} creating new subject: {}", user.getUserId(), request.getSubjectName());
        
        SubjectDTO created = adminService.createSubject(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Subject created successfully", created)
        );
    }

    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectDTO>> updateSubject(
            @PathVariable Long subjectId,
            @Valid @RequestBody CreateSubjectRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} updating subject: {}", user.getUserId(), subjectId);
        
        SubjectDTO updated = adminService.updateSubject(subjectId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Subject updated successfully", updated)
        );
    }

    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable Long subjectId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} deleting subject: {}", user.getUserId(), subjectId);
        
        adminService.deleteSubject(subjectId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Subject deleted successfully", null)
        );
    }

    // ============ Topic Management ============
    
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicDTO>>> getAllTopics(
            @RequestParam(required = false) Long subjectId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} fetching all topics. Subject filter: {}", user.getUserId(), subjectId);
        
        List<TopicDTO> topics = adminService.getAllTopics(subjectId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Topics retrieved successfully", topics)
        );
    }
    
    @PostMapping("/topics")
    public ResponseEntity<ApiResponse<TopicDTO>> createTopic(
            @Valid @RequestBody CreateTopicRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} creating new topic: {}", user.getUserId(), request.getTopicName());
        
        TopicDTO created = adminService.createTopic(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Topic created successfully", created)
        );
    }

    @PutMapping("/topics/{topicId}")
    public ResponseEntity<ApiResponse<TopicDTO>> updateTopic(
            @PathVariable Long topicId,
            @Valid @RequestBody CreateTopicRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} updating topic: {}", user.getUserId(), topicId);
        
        TopicDTO updated = adminService.updateTopic(topicId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Topic updated successfully", updated)
        );
    }

    @DeleteMapping("/topics/{topicId}")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(
            @PathVariable Long topicId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} deleting topic: {}", user.getUserId(), topicId);
        
        adminService.deleteTopic(topicId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Topic deleted successfully", null)
        );
    }

    // ============ Content Management ============
    
    @GetMapping("/contents")
    public ResponseEntity<ApiResponse<List<TopicContentDTO>>> getAllContents(
            @RequestParam(required = false) Long topicId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} fetching all contents. Topic filter: {}", user.getUserId(), topicId);
        
        List<TopicContentDTO> contents = adminService.getAllContents(topicId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Contents retrieved successfully", contents)
        );
    }

    // ============ User Management ============
    
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDetailsDTO>>> getAllUsers(
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} fetching all users. Active filter: {}", user.getUserId(), active);
        
        List<UserDetailsDTO> users = adminService.getAllUsers(active);
        
        return ResponseEntity.ok(
            ApiResponse.success("Users retrieved successfully", users)
        );
    }

    @GetMapping("/users/{customerId}")
    public ResponseEntity<ApiResponse<UserDetailsDTO>> getUserDetails(
            @PathVariable Long customerId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} fetching details for user: {}", user.getUserId(), customerId);
        
        UserDetailsDTO userDetails = adminService.getUserDetails(customerId);
        
        return ResponseEntity.ok(
            ApiResponse.success("User details retrieved successfully", userDetails)
        );
    }

    // ============ Subscription Management ============
    
    @GetMapping("/subscriptions/plans")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDTO>>> getAllSubscriptionPlans(
            @RequestParam(required = false) String targetType,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} fetching subscription plans. Type filter: {}", user.getUserId(), targetType);
        
        List<SubscriptionPlanDTO> plans = adminService.getAllSubscriptionPlans(targetType);
        
        return ResponseEntity.ok(
            ApiResponse.success("Subscription plans retrieved successfully", plans)
        );
    }

    @PostMapping("/subscriptions/plans")
    public ResponseEntity<ApiResponse<SubscriptionPlanDTO>> createSubscriptionPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} creating subscription plan: {}", user.getUserId(), request.getPlanName());
        
        SubscriptionPlanDTO created = adminService.createSubscriptionPlan(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ApiResponse.success("Subscription plan created successfully", created)
        );
    }

    @PutMapping("/subscriptions/plans/{subscriptionId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanDTO>> updateSubscriptionPlan(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody CreateSubscriptionPlanRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} updating subscription plan: {}", user.getUserId(), subscriptionId);
        
        SubscriptionPlanDTO updated = adminService.updateSubscriptionPlan(subscriptionId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Subscription plan updated successfully", updated)
        );
    }

    @DeleteMapping("/subscriptions/plans/{subscriptionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubscriptionPlan(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} deleting subscription plan: {}", user.getUserId(), subscriptionId);
        
        adminService.deleteSubscriptionPlan(subscriptionId);
        
        return ResponseEntity.ok(
            ApiResponse.success("Subscription plan deleted successfully", null)
        );
    }

    @GetMapping("/subscriptions/user-subscriptions")
    public ResponseEntity<ApiResponse<List<UserSubscriptionDTO>>> getAllUserSubscriptions(
            @RequestParam(required = false) Boolean active,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} fetching user subscriptions. Active filter: {}", user.getUserId(), active);
        
        List<UserSubscriptionDTO> subscriptions = adminService.getAllUserSubscriptions(active);
        
        return ResponseEntity.ok(
            ApiResponse.success("User subscriptions retrieved successfully", subscriptions)
        );
    }

    @PutMapping("/subscriptions/user-subscriptions/{subscriptionId}/toggle")
    public ResponseEntity<ApiResponse<UserSubscriptionDTO>> toggleUserSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam String type,
            @AuthenticationPrincipal User user) {
        
        log.info("Admin {} toggling {} subscription: {}", user.getUserId(), type, subscriptionId);
        
        UserSubscriptionDTO updated = adminService.toggleUserSubscription(subscriptionId, type);
        
        return ResponseEntity.ok(
            ApiResponse.success("Subscription status updated successfully", updated)
        );
    }
}
