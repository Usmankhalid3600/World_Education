package com.worldedu.worldeducation.student.service;

import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.auth.entity.UserProfile;
import com.worldedu.worldeducation.auth.repository.UserProfileRepository;
import com.worldedu.worldeducation.auth.repository.UserRepository;
import com.worldedu.worldeducation.student.dto.*;
import com.worldedu.worldeducation.subject.entity.EdClass;
import com.worldedu.worldeducation.subject.entity.EdSubject;
import com.worldedu.worldeducation.subject.entity.UserSubjectSubscription;
import com.worldedu.worldeducation.subject.repository.EdClassRepository;
import com.worldedu.worldeducation.subject.repository.EdSubjectRepository;
import com.worldedu.worldeducation.subject.repository.UserSubjectSubscriptionRepository;
import com.worldedu.worldeducation.subscription.entity.SubscriptionPlan;
import com.worldedu.worldeducation.subscription.repository.SubscriptionPlanRepository;
import com.worldedu.worldeducation.topic.entity.EdTopic;
import com.worldedu.worldeducation.topic.entity.UserTopicSubscription;
import com.worldedu.worldeducation.topic.repository.EdTopicRepository;
import com.worldedu.worldeducation.topic.repository.UserTopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubjectSubscriptionRepository userSubjectSubscriptionRepository;
    private final UserTopicSubscriptionRepository userTopicSubscriptionRepository;
    private final EdClassRepository edClassRepository;
    private final EdSubjectRepository edSubjectRepository;
    private final EdTopicRepository edTopicRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // Get all available subscription plans
    public List<AvailableSubscriptionPlanDTO> getAvailableSubscriptionPlans(Long customerId) {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByIsActiveTrue();
        List<AvailableSubscriptionPlanDTO> result = new ArrayList<>();

        for (SubscriptionPlan plan : plans) {
            AvailableSubscriptionPlanDTO dto = new AvailableSubscriptionPlanDTO();
            dto.setSubscriptionId(plan.getSubscriptionId());
            dto.setPlanName(plan.getPlanName());
            dto.setTargetType(plan.getTargetType().name());
            dto.setTargetId(plan.getTargetId());
            dto.setDurationDays(plan.getDurationDays());
            dto.setPrice(plan.getPrice());
            dto.setCurrency(plan.getCurrency());
            dto.setFreeDays(plan.getFreeDays());
            dto.setGracePeriodDays(plan.getGracePeriodDays());

            // Get target name
            String targetName = getTargetName(plan.getTargetType(), plan.getTargetId());
            dto.setTargetName(targetName);

            // Check if user is already subscribed
            boolean isSubscribed = isUserSubscribed(customerId, plan.getTargetType(), plan.getTargetId());
            dto.setIsSubscribed(isSubscribed);

            result.add(dto);
        }

        return result;
    }

    // Get subscription plans by type
    public List<AvailableSubscriptionPlanDTO> getSubscriptionPlansByType(Long customerId, String type) {
        SubscriptionPlan.TargetType targetType;
        try {
            targetType = SubscriptionPlan.TargetType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid target type: " + type);
        }

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByTargetType(targetType);
        List<AvailableSubscriptionPlanDTO> result = new ArrayList<>();

        for (SubscriptionPlan plan : plans) {
            if (!plan.getIsActive()) continue;

            AvailableSubscriptionPlanDTO dto = new AvailableSubscriptionPlanDTO();
            dto.setSubscriptionId(plan.getSubscriptionId());
            dto.setPlanName(plan.getPlanName());
            dto.setTargetType(plan.getTargetType().name());
            dto.setTargetId(plan.getTargetId());
            dto.setDurationDays(plan.getDurationDays());
            dto.setPrice(plan.getPrice());
            dto.setCurrency(plan.getCurrency());
            dto.setFreeDays(plan.getFreeDays());
            dto.setGracePeriodDays(plan.getGracePeriodDays());

            String targetName = getTargetName(plan.getTargetType(), plan.getTargetId());
            dto.setTargetName(targetName);

            boolean isSubscribed = isUserSubscribed(customerId, plan.getTargetType(), plan.getTargetId());
            dto.setIsSubscribed(isSubscribed);

            result.add(dto);
        }

        return result;
    }

    // Get user's active subscriptions
    public List<MySubscriptionDTO> getMySubscriptions(Long customerId) {
        List<MySubscriptionDTO> result = new ArrayList<>();

        // Get subject subscriptions
        List<UserSubjectSubscription> subjectSubs = userSubjectSubscriptionRepository.findByCustomerId(customerId);
        for (UserSubjectSubscription sub : subjectSubs) {
            MySubscriptionDTO dto = mapToMySubscriptionDTO(sub);
            if (dto != null) {
                result.add(dto);
            }
        }

        // Get topic subscriptions
        List<UserTopicSubscription> topicSubs = userTopicSubscriptionRepository.findByCustomerId(customerId);
        for (UserTopicSubscription sub : topicSubs) {
            MySubscriptionDTO dto = mapToMySubscriptionDTO(sub);
            if (dto != null) {
                result.add(dto);
            }
        }

        return result;
    }

    // Get user profile
    public ProfileDTO getUserProfile(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserProfile profile = userProfileRepository.findById(customerId)
                .orElse(null);

        ProfileDTO dto = new ProfileDTO();
        dto.setCustomerId(user.getCustomerId());
        dto.setUserId(user.getUserId());
        dto.setUserCategory(user.getUserCategory().name());

        if (profile != null) {
            dto.setFirstName(profile.getFirstName());
            dto.setMiddleName(profile.getMiddleName());
            dto.setLastName(profile.getLastName());
            dto.setEmail(profile.getEmail());
            dto.setMobileNo(profile.getMobileNo());
            dto.setCountry(profile.getCountry());
            dto.setState(profile.getState());
            dto.setCity(profile.getCity());
            dto.setAddress(profile.getAddress());
        }

        return dto;
    }

    // Update user profile
    @Transactional
    public ProfileDTO updateUserProfile(Long customerId, UpdateProfileRequest request) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserProfile profile = userProfileRepository.findById(customerId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setCustomerId(customerId);
                    newProfile.setUser(user);
                    return newProfile;
                });

        // Update profile fields
        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getMiddleName() != null) profile.setMiddleName(request.getMiddleName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getEmail() != null) profile.setEmail(request.getEmail());
        if (request.getMobileNo() != null) profile.setMobileNo(request.getMobileNo());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());

        userProfileRepository.save(profile);

        return getUserProfile(customerId);
    }

    // Change password
    @Transactional
    public void changePassword(Long customerId, ChangePasswordRequest request) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Update to new password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // Search topics
    public List<TopicSearchResultDTO> searchTopics(Long customerId, String searchTerm) {
        List<EdTopic> topics = edTopicRepository.searchTopics(searchTerm);
        List<TopicSearchResultDTO> result = new ArrayList<>();

        for (EdTopic topic : topics) {
            TopicSearchResultDTO dto = new TopicSearchResultDTO();
            dto.setTopicId(topic.getTopicId());
            dto.setTopicName(topic.getTopicName());
            dto.setTopicDescription(""); // EdTopic doesn't have description field
            dto.setSubjectId(topic.getSubjectId());

            // Get subject and class info
            Optional<EdSubject> subject = edSubjectRepository.findById(topic.getSubjectId());
            if (subject.isPresent()) {
                dto.setSubjectName(subject.get().getSubjectName());
                dto.setClassId(subject.get().getClassId());

                Optional<EdClass> edClass = edClassRepository.findById(subject.get().getClassId());
                edClass.ifPresent(value -> dto.setClassName(value.getClassName()));
            }

            // Check if user is subscribed to this topic
            boolean isSubscribed = userTopicSubscriptionRepository
                    .findByCustomerIdAndTopicId(customerId, topic.getTopicId())
                    .map(UserTopicSubscription::getIsActive)
                    .orElse(false);
            dto.setIsSubscribed(isSubscribed);

            result.add(dto);
        }

        return result;
    }

    // Helper methods
    private String getTargetName(SubscriptionPlan.TargetType targetType, Long targetId) {
        return switch (targetType) {
            case CLASS -> edClassRepository.findById(targetId)
                    .map(EdClass::getClassName)
                    .orElse("Unknown Class");
            case SUBJECT -> edSubjectRepository.findById(targetId)
                    .map(EdSubject::getSubjectName)
                    .orElse("Unknown Subject");
            case TOPIC -> edTopicRepository.findById(targetId)
                    .map(EdTopic::getTopicName)
                    .orElse("Unknown Topic");
        };
    }

    private boolean isUserSubscribed(Long customerId, SubscriptionPlan.TargetType targetType, Long targetId) {
        return switch (targetType) {
            case SUBJECT -> userSubjectSubscriptionRepository
                    .findByCustomerIdAndSubjectId(customerId, targetId)
                    .map(UserSubjectSubscription::getIsActive)
                    .orElse(false);
            case TOPIC -> userTopicSubscriptionRepository
                    .findByCustomerIdAndTopicId(customerId, targetId)
                    .map(UserTopicSubscription::getIsActive)
                    .orElse(false);
            default -> false;
        };
    }

    private MySubscriptionDTO mapToMySubscriptionDTO(UserSubjectSubscription sub) {
        Optional<EdSubject> subject = edSubjectRepository.findById(sub.getSubjectId());
        if (subject.isEmpty()) return null;

        // Find subscription plan for this subject
        List<SubscriptionPlan> plans = subscriptionPlanRepository
                .findByTargetTypeAndTargetId(SubscriptionPlan.TargetType.SUBJECT, sub.getSubjectId());
        
        SubscriptionPlan plan = plans.isEmpty() ? null : plans.get(0);

        MySubscriptionDTO dto = new MySubscriptionDTO();
        dto.setSubscriptionId(sub.getSubscriptionId());
        dto.setType("SUBJECT");
        dto.setTargetId(sub.getSubjectId());
        dto.setTargetName(subject.get().getSubjectName());
        dto.setSubscribedAt(sub.getSubscribedAt());
        dto.setIsActive(sub.getIsActive());

        if (plan != null) {
            dto.setPrice(plan.getPrice());
            dto.setCurrency(plan.getCurrency());
            dto.setDurationDays(plan.getDurationDays());

            // Calculate expiry and remaining days
            LocalDateTime expiryDate = sub.getSubscribedAt().plusDays(plan.getDurationDays());
            dto.setExpiryDate(expiryDate);

            long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);
            dto.setRemainingDays((int) remainingDays);

            // Determine status
            if (remainingDays > 0) {
                dto.setStatus("ACTIVE");
            } else if (remainingDays >= -plan.getGracePeriodDays()) {
                dto.setStatus("IN_GRACE_PERIOD");
            } else {
                dto.setStatus("EXPIRED");
            }
        } else {
            dto.setStatus(sub.getIsActive() ? "ACTIVE" : "INACTIVE");
        }

        return dto;
    }

    private MySubscriptionDTO mapToMySubscriptionDTO(UserTopicSubscription sub) {
        Optional<EdTopic> topic = edTopicRepository.findById(sub.getTopicId());
        if (topic.isEmpty()) return null;

        // Find subscription plan for this topic
        List<SubscriptionPlan> plans = subscriptionPlanRepository
                .findByTargetTypeAndTargetId(SubscriptionPlan.TargetType.TOPIC, sub.getTopicId());
        
        SubscriptionPlan plan = plans.isEmpty() ? null : plans.get(0);

        MySubscriptionDTO dto = new MySubscriptionDTO();
        dto.setSubscriptionId(sub.getSubscriptionId());
        dto.setType("TOPIC");
        dto.setTargetId(sub.getTopicId());
        dto.setTargetName(topic.get().getTopicName());
        dto.setSubscribedAt(sub.getSubscribedAt());
        dto.setIsActive(sub.getIsActive());

        if (plan != null) {
            dto.setPrice(plan.getPrice());
            dto.setCurrency(plan.getCurrency());
            dto.setDurationDays(plan.getDurationDays());

            // Calculate expiry and remaining days
            LocalDateTime expiryDate = sub.getSubscribedAt().plusDays(plan.getDurationDays());
            dto.setExpiryDate(expiryDate);

            long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), expiryDate);
            dto.setRemainingDays((int) remainingDays);

            // Determine status
            if (remainingDays > 0) {
                dto.setStatus("ACTIVE");
            } else if (remainingDays >= -plan.getGracePeriodDays()) {
                dto.setStatus("IN_GRACE_PERIOD");
            } else {
                dto.setStatus("EXPIRED");
            }
        } else {
            dto.setStatus(sub.getIsActive() ? "ACTIVE" : "INACTIVE");
        }

        return dto;
    }
}
