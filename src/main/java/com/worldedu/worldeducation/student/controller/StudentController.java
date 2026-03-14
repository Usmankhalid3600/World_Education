package com.worldedu.worldeducation.student.controller;

import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.student.dto.*;
import com.worldedu.worldeducation.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal User user) {
        List<AvailableSubscriptionPlanDTO> plans = studentService.getAvailableSubscriptionPlans(user.getCustomerId());
        return ResponseEntity.ok(plans);
    }

    // Get subscription plans by type (CLASS, SUBJECT, TOPIC)
    @GetMapping("/subscription-plans/{type}")
    public ResponseEntity<List<AvailableSubscriptionPlanDTO>> getSubscriptionPlansByType(
            @PathVariable String type,
            @AuthenticationPrincipal User user) {
        List<AvailableSubscriptionPlanDTO> plans = studentService.getSubscriptionPlansByType(user.getCustomerId(), type);
        return ResponseEntity.ok(plans);
    }

    // Get my active subscriptions
    @GetMapping("/my-subscriptions")
    public ResponseEntity<List<MySubscriptionDTO>> getMySubscriptions(@AuthenticationPrincipal User user) {
        List<MySubscriptionDTO> subscriptions = studentService.getMySubscriptions(user.getCustomerId());
        return ResponseEntity.ok(subscriptions);
    }

    // Get user profile
    @GetMapping("/profile")
    public ResponseEntity<ProfileDTO> getUserProfile(@AuthenticationPrincipal User user) {
        ProfileDTO profile = studentService.getUserProfile(user.getCustomerId());
        return ResponseEntity.ok(profile);
    }

    // Update user profile
    @PutMapping("/profile")
    public ResponseEntity<ProfileDTO> updateUserProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        ProfileDTO profile = studentService.updateUserProfile(user.getCustomerId(), request);
        return ResponseEntity.ok(profile);
    }

    // Change password
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User user) {
        try {
            studentService.changePassword(user.getCustomerId(), request);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Search topics
    @GetMapping("/search-topics")
    public ResponseEntity<List<TopicSearchResultDTO>> searchTopics(
            @RequestParam String query,
            @AuthenticationPrincipal User user) {
        List<TopicSearchResultDTO> results = studentService.searchTopics(user.getCustomerId(), query);
        return ResponseEntity.ok(results);
    }
}
