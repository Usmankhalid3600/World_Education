package com.worldedu.worldeducation.topic.service;

import com.worldedu.worldeducation.topic.dto.TopicContentDTO;
import com.worldedu.worldeducation.topic.dto.TopicContentListResponse;
import com.worldedu.worldeducation.topic.dto.TopicDTO;
import com.worldedu.worldeducation.topic.dto.TopicListResponse;
import com.worldedu.worldeducation.topic.entity.EdTopic;
import com.worldedu.worldeducation.topic.entity.TopicContent;
import com.worldedu.worldeducation.topic.entity.UserTopicSubscription;
import com.worldedu.worldeducation.topic.repository.EdTopicRepository;
import com.worldedu.worldeducation.topic.repository.TopicContentRepository;
import com.worldedu.worldeducation.topic.repository.UserTopicSubscriptionRepository;
import com.worldedu.worldeducation.subject.entity.EdSubject;
import com.worldedu.worldeducation.subject.repository.EdSubjectRepository;
import com.worldedu.worldeducation.subject.repository.UserSubjectSubscriptionRepository;
import com.worldedu.worldeducation.subscription.entity.SubscriptionPlan;
import com.worldedu.worldeducation.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TopicService {

    private final EdTopicRepository edTopicRepository;
    private final UserTopicSubscriptionRepository userTopicSubscriptionRepository;
    private final EdSubjectRepository edSubjectRepository;
    private final TopicContentRepository topicContentRepository;
    private final UserSubjectSubscriptionRepository userSubjectSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * Get opted and unopted topics for a subject
     * @param subjectId The subject ID
     * @param customerId The logged-in user's customer ID
     * @return TopicListResponse with opted and unopted topics
     */
    public TopicListResponse getTopicsBySubject(Long subjectId, Long customerId) {
        log.info("Fetching topics for subjectId: {} and customerId: {}", subjectId, customerId);

        // Get subject information
        EdSubject edSubject = edSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + subjectId));

        // Get all active topics for the subject
        List<EdTopic> allTopics = edTopicRepository.findBySubjectIdAndIsActiveTrue(subjectId);

        // Get user's subscribed topics
        List<UserTopicSubscription> userSubscriptions = 
            userTopicSubscriptionRepository.findByCustomerIdAndIsActiveTrue(customerId);
        
        Set<Long> subscribedTopicIds = userSubscriptions.stream()
                .map(UserTopicSubscription::getTopicId)
                .collect(Collectors.toSet());

        // Separate opted and unopted topics
        List<TopicDTO> optedTopics = new ArrayList<>();
        List<TopicDTO> unoptedTopics = new ArrayList<>();

        for (EdTopic topic : allTopics) {
            boolean isOpted = subscribedTopicIds.contains(topic.getTopicId());
            
            TopicDTO.TopicDTOBuilder topicBuilder = TopicDTO.builder()
                    .topicId(topic.getTopicId())
                    .subjectId(topic.getSubjectId())
                    .topicName(topic.getTopicName())
                    .publishDate(topic.getPublishDate())
                    .isActive(topic.getIsActive())
                    .isOpted(isOpted);
            
            // If topic is not opted, include subscription plan information
            if (!isOpted) {
                List<SubscriptionPlan> plans = subscriptionPlanRepository
                        .findByTargetTypeAndTargetId(SubscriptionPlan.TargetType.TOPIC, topic.getTopicId());
                
                if (!plans.isEmpty()) {
                    SubscriptionPlan plan = plans.get(0);
                    topicBuilder
                            .subscriptionPrice(plan.getPrice())
                            .currency(plan.getCurrency())
                            .durationDays(plan.getDurationDays())
                            .freeDays(plan.getFreeDays())
                            .gracePeriodDays(plan.getGracePeriodDays());
                }
            }
            
            TopicDTO topicDTO = topicBuilder.build();

            if (isOpted) {
                optedTopics.add(topicDTO);
            } else {
                unoptedTopics.add(topicDTO);
            }
        }

        log.info("Found {} opted and {} unopted topics for subjectId: {}", 
                optedTopics.size(), unoptedTopics.size(), subjectId);

        return TopicListResponse.builder()
                .subjectId(subjectId)
                .subjectName(edSubject.getSubjectName())
                .optedTopics(optedTopics)
                .unoptedTopics(unoptedTopics)
                .totalTopics(allTopics.size())
                .optedCount(optedTopics.size())
                .unoptedCount(unoptedTopics.size())
                .build();
    }

    /**
     * Get topic contents for a specific topic
     * User must have subscription to either the topic itself OR the parent subject
     * @param topicId The topic ID
     * @param customerId The logged-in user's customer ID
     * @return TopicContentListResponse with all contents if user has access
     */
    public TopicContentListResponse getTopicContents(Long topicId, Long customerId) {
        log.info("Fetching topic contents for topicId: {} and customerId: {}", topicId, customerId);

        // Get topic information
        EdTopic edTopic = edTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));

        // Get subject information
        EdSubject edSubject = edSubjectRepository.findById(edTopic.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + edTopic.getSubjectId()));

        // Check if user has access (either topic subscription OR subject subscription)
        boolean hasTopicSubscription = userTopicSubscriptionRepository
                .existsByCustomerIdAndTopicIdAndIsActiveTrue(customerId, topicId);
        
        boolean hasSubjectSubscription = userSubjectSubscriptionRepository
                .existsByCustomerIdAndSubjectIdAndIsActiveTrue(customerId, edTopic.getSubjectId());

        boolean hasAccess = hasTopicSubscription || hasSubjectSubscription;
        String accessType = null;

        if (hasTopicSubscription) {
            accessType = "TOPIC_SUBSCRIPTION";
            log.info("User {} has direct topic subscription for topicId: {}", customerId, topicId);
        } else if (hasSubjectSubscription) {
            accessType = "SUBJECT_SUBSCRIPTION";
            log.info("User {} has subject subscription for subjectId: {} (includes topicId: {})", 
                    customerId, edTopic.getSubjectId(), topicId);
        }

        List<TopicContentDTO> contentDTOs = new ArrayList<>();

        if (!hasAccess) {
            log.warn("User {} does not have access to topicId: {}", customerId, topicId);
            return TopicContentListResponse.builder()
                    .topicId(topicId)
                    .topicName(edTopic.getTopicName())
                    .subjectId(edSubject.getSubjectId())
                    .subjectName(edSubject.getSubjectName())
                    .hasAccess(false)
                    .accessType(null)
                    .contents(contentDTOs)
                    .totalContents(0)
                    .build();
        }

        // User has access - fetch all active topic contents
        List<TopicContent> topicContents = topicContentRepository.findByTopicIdAndIsActiveTrue(topicId);

        // Convert to DTOs
        contentDTOs = topicContents.stream()
                .map(content -> {
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
                    // Set binary data (will be automatically converted to Base64)
                    dto.setTopicContentData(content.getTopicContentData());
                    return dto;
                })
                .collect(Collectors.toList());

        log.info("User {} has access to {} contents for topicId: {} via {}", 
                customerId, contentDTOs.size(), topicId, accessType);

        return TopicContentListResponse.builder()
                .topicId(topicId)
                .topicName(edTopic.getTopicName())
                .subjectId(edSubject.getSubjectId())
                .subjectName(edSubject.getSubjectName())
                .hasAccess(true)
                .accessType(accessType)
                .contents(contentDTOs)
                .totalContents(contentDTOs.size())
                .build();
    }

    /**
     * Upload topic content with file data (MultipartFile)
     * Supports uploading files as binary data (BLOB)
     * @param topicId The topic ID
     * @param file The uploaded file
     * @param customerId The logged-in user's customer ID
     * @return The saved TopicContent entity
     */
    @Transactional
    public TopicContent uploadTopicContent(Long topicId, MultipartFile file, Long customerId) throws IOException {
        log.info("Uploading content for topicId: {} by customerId: {}", topicId, customerId);

        // Validate topic exists
        edTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Get file details
        String originalFilename = file.getOriginalFilename();
        String fileType = getFileExtension(originalFilename);
        byte[] fileData = file.getBytes();

        // Create and save topic content
        TopicContent topicContent = new TopicContent();
        topicContent.setTopicId(topicId);
        topicContent.setFileName(originalFilename);
        topicContent.setFileType(fileType.toUpperCase());
        topicContent.setTopicContentData(fileData);
        topicContent.setUploadedBy(customerId);
        topicContent.setUploadedAt(LocalDateTime.now());
        topicContent.setIsActive(true);

        TopicContent saved = topicContentRepository.save(topicContent);
        log.info("Content uploaded successfully. ContentId: {}, Size: {} bytes", 
                saved.getContentId(), fileData.length);

        return saved;
    }

    /**
     * Upload topic content with Base64 encoded data
     * Useful for JSON-based uploads
     * @param topicId The topic ID
     * @param fileName The file name
     * @param fileType The file type (PDF, JPG, PNG, etc.)
     * @param base64Data Base64 encoded file data
     * @param customerId The logged-in user's customer ID
     * @return The saved TopicContent entity
     */
    @Transactional
    public TopicContent uploadTopicContentBase64(Long topicId, String fileName, String fileType, 
                                                 String base64Data, Long customerId) {
        log.info("Uploading Base64 content for topicId: {} by customerId: {}", topicId, customerId);

        // Validate topic exists
        edTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found with id: " + topicId));

        // Decode Base64 data
        byte[] fileData;
        try {
            fileData = Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 data", e);
        }

        // Create and save topic content
        TopicContent topicContent = new TopicContent();
        topicContent.setTopicId(topicId);
        topicContent.setFileName(fileName);
        topicContent.setFileType(fileType.toUpperCase());
        topicContent.setTopicContentData(fileData);
        topicContent.setUploadedBy(customerId);
        topicContent.setUploadedAt(LocalDateTime.now());
        topicContent.setIsActive(true);

        TopicContent saved = topicContentRepository.save(topicContent);
        log.info("Base64 content uploaded successfully. ContentId: {}, Size: {} bytes", 
                saved.getContentId(), fileData.length);

        return saved;
    }

    /**
     * Delete topic content (soft delete)
     * @param contentId The content ID
     * @param customerId The logged-in user's customer ID (for audit)
     */
    @Transactional
    public void deleteTopicContent(Long contentId, Long customerId) {
        log.info("Deleting content {} by customerId: {}", contentId, customerId);

        TopicContent content = topicContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found with id: " + contentId));

        content.setIsActive(false);
        topicContentRepository.save(content);

        log.info("Content {} soft deleted successfully", contentId);
    }

    /**
     * Helper method to extract file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "UNKNOWN";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
