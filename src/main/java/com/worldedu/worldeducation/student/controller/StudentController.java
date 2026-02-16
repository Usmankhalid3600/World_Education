package com.worldedu.worldeducation.student.controller;

import com.worldedu.worldeducation.student.dto.*;
import com.worldedu.worldeducation.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    private final StudentService studentService;

    // Get all available subscription plans
    @GetMapping("/subscription-plans")
    public ResponseEntity<List<AvailableSubscriptionPlanDTO>> getAvailableSubscriptionPlans(
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        List<AvailableSubscriptionPlanDTO> plans = studentService.getAvailableSubscriptionPlans(customerId);
        return ResponseEntity.ok(plans);
    }

    // Get subscription plans by type (CLASS, SUBJECT, TOPIC)
    @GetMapping("/subscription-plans/{type}")
    public ResponseEntity<List<AvailableSubscriptionPlanDTO>> getSubscriptionPlansByType(
            @PathVariable String type,
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        List<AvailableSubscriptionPlanDTO> plans = studentService.getSubscriptionPlansByType(customerId, type);
        return ResponseEntity.ok(plans);
    }

    // Get my active subscriptions
    @GetMapping("/my-subscriptions")
    public ResponseEntity<List<MySubscriptionDTO>> getMySubscriptions(Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        List<MySubscriptionDTO> subscriptions = studentService.getMySubscriptions(customerId);
        return ResponseEntity.ok(subscriptions);
    }

    // Get user profile
    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getUserProfile(Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        ProfileDTO profile = studentService.getUserProfile(customerId);
        return ResponseEntity.ok(profile);
    }

    // Update user profile
    @PutMapping("/profile")
    public ResponseEntity<ProfileDTO> updateUserProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        ProfileDTO profile = studentService.updateUserProfile(customerId, request);
        return ResponseEntity.ok(profile);
    }

    // Change password
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        try {
            Long customerId = Long.parseLong(authentication.getName());
            studentService.changePassword(customerId, request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Search topics
    @GetMapping("/search-topics")
    public ResponseEntity<List<TopicSearchResultDTO>> searchTopics(
            @RequestParam String query,
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        List<TopicSearchResultDTO> results = studentService.searchTopics(customerId, query);
        return ResponseEntity.ok(results);
    }
}
