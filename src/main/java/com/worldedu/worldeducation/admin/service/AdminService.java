package com.worldedu.worldeducation.admin.service;

import com.worldedu.worldeducation.admin.dto.*;
import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.auth.entity.UserProfile;
import com.worldedu.worldeducation.auth.repository.UserProfileRepository;
import com.worldedu.worldeducation.auth.repository.UserRepository;
import com.worldedu.worldeducation.enums.UserCategory;
import com.worldedu.worldeducation.subject.dto.ClassDTO;
import com.worldedu.worldeducation.subject.dto.SubjectDTO;
import com.worldedu.worldeducation.subject.entity.EdClass;
import com.worldedu.worldeducation.subject.entity.EdSubject;
import com.worldedu.worldeducation.subject.entity.UserSubjectSubscription;
import com.worldedu.worldeducation.subject.repository.EdClassRepository;
import com.worldedu.worldeducation.subject.repository.EdSubjectRepository;
import com.worldedu.worldeducation.subject.repository.UserSubjectSubscriptionRepository;
import com.worldedu.worldeducation.subscription.dto.CreateSubscriptionPlanRequest;
import com.worldedu.worldeducation.subscription.dto.SubscriptionPlanDTO;
import com.worldedu.worldeducation.subscription.dto.UserSubscriptionDTO;
import com.worldedu.worldeducation.subscription.entity.SubscriptionPlan;
import com.worldedu.worldeducation.subscription.repository.SubscriptionPlanRepository;
import com.worldedu.worldeducation.topic.dto.TopicDTO;
import com.worldedu.worldeducation.topic.dto.TopicContentDTO;
import com.worldedu.worldeducation.topic.entity.EdTopic;
import com.worldedu.worldeducation.topic.entity.TopicContent;
import com.worldedu.worldeducation.topic.entity.UserTopicSubscription;
import com.worldedu.worldeducation.topic.repository.EdTopicRepository;
import com.worldedu.worldeducation.topic.repository.TopicContentRepository;
import com.worldedu.worldeducation.topic.repository.UserTopicSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final EdClassRepository classRepository;
    private final EdSubjectRepository subjectRepository;
    private final EdTopicRepository topicRepository;
    private final TopicContentRepository topicContentRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSubjectSubscriptionRepository subjectSubscriptionRepository;
    private final UserTopicSubscriptionRepository topicSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    // ============ Class Management ============

    @Transactional
    public ClassDTO createClass(CreateClassRequest request) {
        EdClass edClass = new EdClass();
        edClass.setClassName(request.getClassName());
        edClass.setClassNumber(request.getClassNumber());
        edClass.setIsActive(request.getIsActive());
        
        EdClass saved = classRepository.save(edClass);
        log.info("Created class: {}", saved.getClassName());
        
        return mapToClassDTO(saved);
    }

    @Transactional
    public ClassDTO updateClass(Long classId, CreateClassRequest request) {
        EdClass edClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + classId));
        
        edClass.setClassName(request.getClassName());
        edClass.setClassNumber(request.getClassNumber());
        edClass.setIsActive(request.getIsActive());
        
        EdClass updated = classRepository.save(edClass);
        log.info("Updated class: {}", updated.getClassName());
        
        return mapToClassDTO(updated);
    }

    @Transactional
    public void deleteClass(Long classId) {
        EdClass edClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + classId));
        
        classRepository.delete(edClass);
        log.info("Deleted class: {}", edClass.getClassName());
    }

    // ============ Subject Management ============

    public List<SubjectDTO> getAllSubjects(Long classId) {
        List<EdSubject> subjects;
        
        if (classId != null) {
            subjects = subjectRepository.findByClassId(classId);
            log.info("Fetched {} subjects for classId: {}", subjects.size(), classId);
        } else {
            subjects = subjectRepository.findAll();
            log.info("Fetched all {} subjects", subjects.size());
        }
        
        return subjects.stream()
                .map(this::mapToSubjectDTO)
                .toList();
    }

    @Transactional
    public SubjectDTO createSubject(CreateSubjectRequest request) {
        // Verify class exists
        classRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + request.getClassId()));
        
        EdSubject subject = new EdSubject();
        subject.setClassId(request.getClassId());
        subject.setSubjectName(request.getSubjectName());
        subject.setIsActive(request.getIsActive());
        
        EdSubject saved = subjectRepository.save(subject);
        log.info("Created subject: {}", saved.getSubjectName());
        
        return mapToSubjectDTO(saved);
    }

    @Transactional
    public SubjectDTO updateSubject(Long subjectId, CreateSubjectRequest request) {
        EdSubject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + subjectId));
        
        subject.setClassId(request.getClassId());
        subject.setSubjectName(request.getSubjectName());
        subject.setIsActive(request.getIsActive());
        
        EdSubject updated = subjectRepository.save(subject);
        log.info("Updated subject: {}", updated.getSubjectName());
        
        return mapToSubjectDTO(updated);
    }

    @Transactional
    public void deleteSubject(Long subjectId) {
        EdSubject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + subjectId));
        
        subjectRepository.delete(subject);
        log.info("Deleted subject: {}", subject.getSubjectName());
    }

    // ============ Topic Management ============

    public List<TopicDTO> getAllTopics(Long subjectId) {
        List<EdTopic> topics;
        
        if (subjectId != null) {
            topics = topicRepository.findBySubjectId(subjectId);
            log.info("Fetched {} topics for subjectId: {}", topics.size(), subjectId);
        } else {
            topics = topicRepository.findAll();
            log.info("Fetched all {} topics", topics.size());
        }
        
        return topics.stream()
                .map(this::mapToTopicDTO)
                .toList();
    }

    @Transactional
    public TopicDTO createTopic(CreateTopicRequest request) {
        // Verify subject exists
        subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + request.getSubjectId()));
        
        EdTopic topic = new EdTopic();
        topic.setSubjectId(request.getSubjectId());
        topic.setTopicName(request.getTopicName());
        topic.setPublishDate(request.getPublishDate() != null ? request.getPublishDate() : LocalDateTime.now());
        topic.setIsActive(request.getIsActive());
        
        EdTopic saved = topicRepository.save(topic);
        log.info("Created topic: {}", saved.getTopicName());
        
        return mapToTopicDTO(saved);
    }

    @Transactional
    public TopicDTO updateTopic(Long topicId, CreateTopicRequest request) {
        EdTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        topic.setSubjectId(request.getSubjectId());
        topic.setTopicName(request.getTopicName());
        if (request.getPublishDate() != null) {
            topic.setPublishDate(request.getPublishDate());
        }
        topic.setIsActive(request.getIsActive());
        
        EdTopic updated = topicRepository.save(topic);
        log.info("Updated topic: {}", updated.getTopicName());
        
        return mapToTopicDTO(updated);
    }

    @Transactional
    public void deleteTopic(Long topicId) {
        EdTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));
        
        topicRepository.delete(topic);
        log.info("Deleted topic: {}", topic.getTopicName());
    }

    // ============ Content Management ============

    public List<TopicContentDTO> getAllContents(Long topicId) {
        List<TopicContent> contents;
        
        if (topicId != null) {
            contents = topicContentRepository.findByTopicId(topicId);
            log.info("Fetched {} contents for topicId: {}", contents.size(), topicId);
        } else {
            contents = topicContentRepository.findAll();
            log.info("Fetched all {} contents", contents.size());
        }
        
        return contents.stream()
                .map(this::mapToTopicContentDTO)
                .toList();
    }

    // ============ User Management ============

    public List<UserDetailsDTO> getAllUsers(Boolean active) {
        List<User> users;
        
        if (active != null) {
            users = userRepository.findAll().stream()
                    .filter(user -> active.equals(!user.getAccountLocked()))
                    .collect(Collectors.toList());
        } else {
            users = userRepository.findAll();
        }
        
        return users.stream()
                .map(this::mapToUserDetailsDTO)
                .collect(Collectors.toList());
    }

    public UserDetailsDTO getUserDetails(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + customerId));
        
        return mapToUserDetailsDTO(user);
    }

    // ============ Helper Methods ============

    private ClassDTO mapToClassDTO(EdClass edClass) {
        return ClassDTO.builder()
                .classId(edClass.getClassId())
                .className(edClass.getClassName())
                .classNumber(edClass.getClassNumber())
                .isActive(edClass.getIsActive())
                .build();
    }

    private SubjectDTO mapToSubjectDTO(EdSubject subject) {
        return SubjectDTO.builder()
                .subjectId(subject.getSubjectId())
                .classId(subject.getClassId())
                .subjectName(subject.getSubjectName())
                .isActive(subject.getIsActive())
                .isOpted(false)
                .build();
    }

    private TopicDTO mapToTopicDTO(EdTopic topic) {
        return TopicDTO.builder()
                .topicId(topic.getTopicId())
                .subjectId(topic.getSubjectId())
                .topicName(topic.getTopicName())
                .publishDate(topic.getPublishDate())
                .isActive(topic.getIsActive())
                .isOpted(false)
                .build();
    }

    private TopicContentDTO mapToTopicContentDTO(TopicContent content) {
        TopicContentDTO dto = TopicContentDTO.builder()
                .contentId(content.getContentId())
                .topicId(content.getTopicId())
                .fileName(content.getFileName())
                .filePathUrl(content.getFilePathUrl())
                .fileType(content.getFileType())
                .uploadedBy(content.getUploadedBy())
                .uploadedAt(content.getUploadedAt())
                .isActive(content.getIsActive())
                .build();
        
        // Set binary data (will be auto-converted to Base64)
        if (content.getTopicContentData() != null) {
            dto.setTopicContentData(content.getTopicContentData());
        }
        
        return dto;
    }

    private UserDetailsDTO mapToUserDetailsDTO(User user) {
        UserProfile profile = userProfileRepository.findById(user.getCustomerId()).orElse(null);
        
        // Count opted subjects and topics
        int optedSubjects = subjectSubscriptionRepository.findByCustomerIdAndIsActiveTrue(user.getCustomerId()).size();
        int optedTopics = topicSubscriptionRepository.findByCustomerIdAndIsActiveTrue(user.getCustomerId()).size();
        
        return UserDetailsDTO.builder()
                .customerId(user.getCustomerId())
                .userId(user.getUserId())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .email(profile != null ? profile.getEmail() : null)
                .mobileNo(profile != null ? profile.getMobileNo() : null)
                .userCategory(user.getUserCategory())
                .accountLocked(user.getAccountLocked())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .totalOptedSubjects(optedSubjects)
                .totalOptedTopics(optedTopics)
                .build();
    }

    // ============ Subscription Management ============

    public List<SubscriptionPlanDTO> getAllSubscriptionPlans(String targetType) {
        List<SubscriptionPlan> plans;
        
        if (targetType != null && !targetType.isEmpty()) {
            plans = subscriptionPlanRepository.findByTargetType(
                SubscriptionPlan.TargetType.valueOf(targetType.toUpperCase())
            );
        } else {
            plans = subscriptionPlanRepository.findAll();
        }
        
        return plans.stream()
                .map(this::mapToSubscriptionPlanDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionPlanDTO createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanName(request.getPlanName());
        plan.setTargetType(request.getTargetType());
        plan.setTargetId(request.getTargetId());
        plan.setDurationDays(request.getDurationDays());
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        plan.setGracePeriodDays(request.getGracePeriodDays());
        plan.setFreeDays(request.getFreeDays());
        plan.setIsActive(request.getIsActive());
        
        SubscriptionPlan saved = subscriptionPlanRepository.save(plan);
        log.info("Created subscription plan: {}", saved.getPlanName());
        
        return mapToSubscriptionPlanDTO(saved);
    }

    @Transactional
    public SubscriptionPlanDTO updateSubscriptionPlan(Long subscriptionId, CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with id: " + subscriptionId));
        
        plan.setPlanName(request.getPlanName());
        plan.setTargetType(request.getTargetType());
        plan.setTargetId(request.getTargetId());
        plan.setDurationDays(request.getDurationDays());
        plan.setPrice(request.getPrice());
        plan.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        plan.setGracePeriodDays(request.getGracePeriodDays());
        plan.setFreeDays(request.getFreeDays());
        plan.setIsActive(request.getIsActive());
        
        SubscriptionPlan updated = subscriptionPlanRepository.save(plan);
        log.info("Updated subscription plan: {}", updated.getPlanName());
        
        return mapToSubscriptionPlanDTO(updated);
    }

    @Transactional
    public void deleteSubscriptionPlan(Long subscriptionId) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with id: " + subscriptionId));
        
        subscriptionPlanRepository.delete(plan);
        log.info("Deleted subscription plan: {}", plan.getPlanName());
    }

    public List<UserSubscriptionDTO> getAllUserSubscriptions(Boolean active) {
        List<UserSubscriptionDTO> result = new ArrayList<>();
        
        // Get subject subscriptions
        List<UserSubjectSubscription> subjectSubs;
        if (active != null) {
            if (active) {
                subjectSubs = subjectSubscriptionRepository.findAll().stream()
                        .filter(sub -> sub.getIsActive())
                        .collect(Collectors.toList());
            } else {
                subjectSubs = subjectSubscriptionRepository.findAll().stream()
                        .filter(sub -> !sub.getIsActive())
                        .collect(Collectors.toList());
            }
        } else {
            subjectSubs = subjectSubscriptionRepository.findAll();
        }
        
        for (UserSubjectSubscription sub : subjectSubs) {
            result.add(mapToUserSubscriptionDTO(sub));
        }
        
        // Get topic subscriptions
        List<UserTopicSubscription> topicSubs;
        if (active != null) {
            if (active) {
                topicSubs = topicSubscriptionRepository.findAll().stream()
                        .filter(sub -> sub.getIsActive())
                        .collect(Collectors.toList());
            } else {
                topicSubs = topicSubscriptionRepository.findAll().stream()
                        .filter(sub -> !sub.getIsActive())
                        .collect(Collectors.toList());
            }
        } else {
            topicSubs = topicSubscriptionRepository.findAll();
        }
        
        for (UserTopicSubscription sub : topicSubs) {
            result.add(mapToUserSubscriptionDTO(sub));
        }
        
        return result;
    }

    @Transactional
    public UserSubscriptionDTO toggleUserSubscription(Long subscriptionId, String type) {
        if ("SUBJECT".equalsIgnoreCase(type)) {
            UserSubjectSubscription subscription = subjectSubscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Subject subscription not found with id: " + subscriptionId));
            
            subscription.setIsActive(!subscription.getIsActive());
            UserSubjectSubscription updated = subjectSubscriptionRepository.save(subscription);
            log.info("Toggled subject subscription: {} to {}", subscriptionId, updated.getIsActive());
            
            return mapToUserSubscriptionDTO(updated);
        } else if ("TOPIC".equalsIgnoreCase(type)) {
            UserTopicSubscription subscription = topicSubscriptionRepository.findById(subscriptionId)
                    .orElseThrow(() -> new RuntimeException("Topic subscription not found with id: " + subscriptionId));
            
            subscription.setIsActive(!subscription.getIsActive());
            UserTopicSubscription updated = topicSubscriptionRepository.save(subscription);
            log.info("Toggled topic subscription: {} to {}", subscriptionId, updated.getIsActive());
            
            return mapToUserSubscriptionDTO(updated);
        }
        
        throw new RuntimeException("Invalid subscription type: " + type);
    }

    // ============ Helper Methods for Subscriptions ============

    private SubscriptionPlanDTO mapToSubscriptionPlanDTO(SubscriptionPlan plan) {
        String targetName = getTargetName(plan.getTargetType(), plan.getTargetId());
        
        return SubscriptionPlanDTO.builder()
                .subscriptionId(plan.getSubscriptionId())
                .planName(plan.getPlanName())
                .targetType(plan.getTargetType())
                .targetId(plan.getTargetId())
                .targetName(targetName)
                .durationDays(plan.getDurationDays())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .gracePeriodDays(plan.getGracePeriodDays())
                .freeDays(plan.getFreeDays())
                .isActive(plan.getIsActive())
                .build();
    }

    private String getTargetName(SubscriptionPlan.TargetType targetType, Long targetId) {
        switch (targetType) {
            case CLASS:
                return classRepository.findById(targetId)
                        .map(EdClass::getClassName)
                        .orElse("Unknown Class");
            case SUBJECT:
                return subjectRepository.findById(targetId)
                        .map(EdSubject::getSubjectName)
                        .orElse("Unknown Subject");
            case TOPIC:
                return topicRepository.findById(targetId)
                        .map(EdTopic::getTopicName)
                        .orElse("Unknown Topic");
            default:
                return "Unknown";
        }
    }

    private UserSubscriptionDTO mapToUserSubscriptionDTO(UserSubjectSubscription sub) {
        User user = userRepository.findById(sub.getCustomerId()).orElse(null);
        UserProfile profile = user != null ? userProfileRepository.findById(user.getCustomerId()).orElse(null) : null;
        EdSubject subject = subjectRepository.findById(sub.getSubjectId()).orElse(null);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return UserSubscriptionDTO.builder()
                .subscriptionId(sub.getSubscriptionId())
                .customerId(sub.getCustomerId())
                .userId(user != null ? user.getUserId() : null)
                .userName(profile != null ? profile.getFirstName() + " " + profile.getLastName() : null)
                .subjectId(sub.getSubjectId())
                .subjectName(subject != null ? subject.getSubjectName() : null)
                .subscribedAt(sub.getSubscribedAt() != null ? sub.getSubscribedAt().format(formatter) : null)
                .isActive(sub.getIsActive())
                .build();
    }

    private UserSubscriptionDTO mapToUserSubscriptionDTO(UserTopicSubscription sub) {
        User user = userRepository.findById(sub.getCustomerId()).orElse(null);
        UserProfile profile = user != null ? userProfileRepository.findById(user.getCustomerId()).orElse(null) : null;
        EdTopic topic = topicRepository.findById(sub.getTopicId()).orElse(null);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return UserSubscriptionDTO.builder()
                .subscriptionId(sub.getSubscriptionId())
                .customerId(sub.getCustomerId())
                .userId(user != null ? user.getUserId() : null)
                .userName(profile != null ? profile.getFirstName() + " " + profile.getLastName() : null)
                .topicId(sub.getTopicId())
                .topicName(topic != null ? topic.getTopicName() : null)
                .subscribedAt(sub.getSubscribedAt() != null ? sub.getSubscribedAt().format(formatter) : null)
                .isActive(sub.getIsActive())
                .build();
    }
}
