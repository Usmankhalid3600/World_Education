package com.worldedu.worldeducation.topic.service;

import com.worldedu.worldeducation.topic.dto.TopicContentDTO;
import com.worldedu.worldeducation.topic.dto.TopicContentListResponse;
import com.worldedu.worldeducation.topic.dto.TopicDTO;
import com.worldedu.worldeducation.topic.dto.TopicListResponse;
import com.worldedu.worldeducation.topic.dto.TopicSubscriptionOptionsDTO;
import com.worldedu.worldeducation.topic.entity.EdTopic;
import com.worldedu.worldeducation.topic.entity.TopicContent;
import com.worldedu.worldeducation.topic.entity.UserTopicSubscription;
import com.worldedu.worldeducation.topic.repository.EdTopicRepository;
import com.worldedu.worldeducation.topic.repository.TopicContentRepository;
import com.worldedu.worldeducation.topic.repository.UserTopicSubscriptionRepository;
import com.worldedu.worldeducation.subject.entity.EdClass;
import com.worldedu.worldeducation.subject.entity.EdSubject;
import com.worldedu.worldeducation.subject.entity.UserSubjectSubscription;
import com.worldedu.worldeducation.subject.repository.EdClassRepository;
import com.worldedu.worldeducation.subject.repository.EdSubjectRepository;
import com.worldedu.worldeducation.subject.repository.UserSubjectSubscriptionRepository;
import com.worldedu.worldeducation.enums.UserCategory;
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
    private final EdClassRepository edClassRepository;
    private final TopicContentRepository topicContentRepository;
    private final UserSubjectSubscriptionRepository userSubjectSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    /**
     * Get opted and unopted topics for a subject
     * @param subjectId The subject ID
     * @param customerId The logged-in user's customer ID
     * @param userCategory The caller's role — ADMIN bypasses subscription/price filtering
     * @return TopicListResponse with opted and unopted topics
     */
    public TopicListResponse getTopicsBySubject(Long subjectId, Long customerId, UserCategory userCategory) {
        log.info("Fetching topics for subjectId: {} and customerId: {}", subjectId, customerId);

        // Get subject information
        EdSubject edSubject = edSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + subjectId));

        // ADMIN sees all topics (including inactive); students see only active ones
        List<EdTopic> allTopics = (userCategory == UserCategory.ADMIN)
                ? edTopicRepository.findBySubjectId(subjectId)
                : edTopicRepository.findBySubjectIdAndIsActiveTrue(subjectId);

        // Check if user has an active subject subscription — grants access to ALL topics in this subject
        boolean hasSubjectSubscription = userSubjectSubscriptionRepository
                .existsByCustomerIdAndSubjectIdAndIsActiveTrue(customerId, subjectId);

        // Check if user had a subject subscription that is now inactive (deactivated by admin)
        boolean hasInactiveSubjectSubscription = !hasSubjectSubscription && userCategory != UserCategory.ADMIN
                && userSubjectSubscriptionRepository
                        .existsByCustomerIdAndSubjectIdAndIsActiveFalse(customerId, subjectId);

        // Get user's individually subscribed topics (active)
        Set<Long> subscribedTopicIds;
        if (hasSubjectSubscription) {
            // Subject subscription covers everything — no need to query individual topic subscriptions
            subscribedTopicIds = allTopics.stream()
                    .map(EdTopic::getTopicId)
                    .collect(Collectors.toSet());
        } else {
            List<UserTopicSubscription> userSubscriptions =
                    userTopicSubscriptionRepository.findByCustomerIdAndIsActiveTrue(customerId);
            subscribedTopicIds = userSubscriptions.stream()
                    .map(UserTopicSubscription::getTopicId)
                    .collect(Collectors.toSet());
        }

        // Get user's individually subscribed topics that are now inactive
        Set<Long> inactiveTopicIds;
        if (hasSubjectSubscription || userCategory == UserCategory.ADMIN) {
            inactiveTopicIds = Set.of();
        } else {
            List<UserTopicSubscription> inactiveSubs =
                    userTopicSubscriptionRepository.findByCustomerIdAndIsActiveFalse(customerId);
            inactiveTopicIds = inactiveSubs.stream()
                    .map(UserTopicSubscription::getTopicId)
                    .collect(Collectors.toSet());
        }

        // Separate opted and unopted topics
        List<TopicDTO> optedTopics = new ArrayList<>();
        List<TopicDTO> unoptedTopics = new ArrayList<>();

        for (EdTopic topic : allTopics) {
            boolean isOpted = subscribedTopicIds.contains(topic.getTopicId());
            boolean subscriptionInactive = !isOpted
                    && (hasInactiveSubjectSubscription || inactiveTopicIds.contains(topic.getTopicId()));

            TopicDTO.TopicDTOBuilder topicBuilder = TopicDTO.builder()
                    .topicId(topic.getTopicId())
                    .subjectId(topic.getSubjectId())
                    .topicName(topic.getTopicName())
                    .publishDate(topic.getPublishDate())
                    .isActive(topic.getIsActive())
                    .isOpted(isOpted)
                    .subscriptionInactive(subscriptionInactive)
                    .description(topic.getDescription());

            // For unopted topics with no prior subscription: apply plan/price filter for students.
            // Topics with inactive subscriptions are always shown so student knows access was revoked.
            if (!isOpted && !subscriptionInactive && userCategory != UserCategory.ADMIN) {
                List<SubscriptionPlan> topicPlans = subscriptionPlanRepository.findByTopicId(topic.getTopicId());
                SubscriptionPlan activePlan = topicPlans.stream()
                        .filter(SubscriptionPlan::getIsActive)
                        .findFirst()
                        .orElse(null);

                if (activePlan == null) {
                    log.debug("Hiding topic {} — no topic-level subscription plan set", topic.getTopicName());
                    continue;
                }

                // Populate topic's own plan pricing
                topicBuilder
                        .subscriptionPrice(activePlan.getPrice())
                        .currency(activePlan.getCurrency())
                        .durationDays(activePlan.getDurationDays())
                        .freeDays(activePlan.getFreeDays())
                        .gracePeriodDays(activePlan.getGracePeriodDays());
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
    public TopicContentListResponse getTopicContents(Long topicId, Long customerId, UserCategory userCategory) {
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

        // ADMIN always has full access — bypass subscription check
        if (userCategory == UserCategory.ADMIN) {
            hasAccess = true;
            accessType = "ADMIN";
        }

        if (hasTopicSubscription) {
            accessType = "TOPIC_SUBSCRIPTION";
            log.info("User {} has direct topic subscription for topicId: {}", customerId, topicId);
        } else if (hasSubjectSubscription) {
            accessType = "SUBJECT_SUBSCRIPTION";
            log.info("User {} has subject subscription for subjectId: {} (includes topicId: {})", 
                    customerId, edTopic.getSubjectId(), topicId);
        }

        // ADMIN sees all content items (including inactive); students see only active ones
        List<TopicContent> topicContents = (userCategory == UserCategory.ADMIN)
                ? topicContentRepository.findByTopicId(topicId)
                : topicContentRepository.findByTopicIdAndIsActiveTrue(topicId);

        long freeCount = topicContents.stream().filter(c -> Boolean.TRUE.equals(c.getIsFree())).count();
        long lockedCount = topicContents.size() - freeCount;
        boolean hasFreeContent = freeCount > 0;

        if (!hasAccess) {
            log.warn("User {} does not have access to topicId: {}. Free items: {}", customerId, topicId, freeCount);

            // Return only free items — user can preview them without subscription
            List<TopicContentDTO> freeContentDTOs = topicContents.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsFree()))
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
                                .isFree(true)
                                .build();
                        dto.setTopicContentData(content.getTopicContentData());
                        return dto;
                    })
                    .collect(Collectors.toList());

            return TopicContentListResponse.builder()
                    .topicId(topicId)
                    .topicName(edTopic.getTopicName())
                    .subjectId(edSubject.getSubjectId())
                    .subjectName(edSubject.getSubjectName())
                    .hasAccess(false)
                    .accessType(null)
                    .contents(freeContentDTOs)
                    .totalContents(freeContentDTOs.size())
                    .hasFreeContent(hasFreeContent)
                    .freeContentsCount((int) freeCount)
                    .lockedContentsCount((int) lockedCount)
                    .build();
        }

        // User has full access — return all active contents
        List<TopicContentDTO> contentDTOs = topicContents.stream()
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
                            .isFree(content.getIsFree())
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
                .hasFreeContent(hasFreeContent)
                .freeContentsCount((int) freeCount)
                .lockedContentsCount((int) lockedCount)
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
    public TopicContent uploadTopicContent(Long topicId, MultipartFile file, Long customerId, Boolean isFree) throws IOException {
        log.info("Uploading content for topicId: {} by customerId: {}, isFree: {}", topicId, customerId, isFree);

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
        topicContent.setIsFree(Boolean.TRUE.equals(isFree));

        TopicContent saved = topicContentRepository.save(topicContent);
        log.info("Content uploaded successfully. ContentId: {}, Size: {} bytes, isFree: {}",
                saved.getContentId(), fileData.length, saved.getIsFree());

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
                                                 String base64Data, Long customerId, Boolean isFree) {
        log.info("Uploading Base64 content for topicId: {} by customerId: {}, isFree: {}", topicId, customerId, isFree);

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
        topicContent.setIsFree(Boolean.TRUE.equals(isFree));

        TopicContent saved = topicContentRepository.save(topicContent);
        log.info("Base64 content uploaded successfully. ContentId: {}, Size: {} bytes, isFree: {}",
                saved.getContentId(), fileData.length, saved.getIsFree());

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
     * Returns all available subscription plans grouped by level (topic / subject / class)
     * for a given topic. Used to show the subscription options screen when a student
     * tries to access a locked topic.
     */
    public TopicSubscriptionOptionsDTO getTopicSubscriptionOptions(Long topicId) {
        EdTopic topic = edTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found: " + topicId));

        EdSubject subject = edSubjectRepository.findById(topic.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found: " + topic.getSubjectId()));

        EdClass cls = edClassRepository.findById(subject.getClassId())
                .orElseThrow(() -> new RuntimeException("Class not found: " + subject.getClassId()));

        List<TopicSubscriptionOptionsDTO.PlanOption> topicPlans =
                subscriptionPlanRepository.findByTopicId(topicId).stream()
                        .filter(SubscriptionPlan::getIsActive)
                        .map(p -> toPlanOption(p, "TOPIC", topicId))
                        .collect(Collectors.toList());

        List<TopicSubscriptionOptionsDTO.PlanOption> subjectPlans =
                subscriptionPlanRepository.findBySubjectId(topic.getSubjectId()).stream()
                        .filter(SubscriptionPlan::getIsActive)
                        .map(p -> toPlanOption(p, "SUBJECT", topic.getSubjectId()))
                        .collect(Collectors.toList());

        List<TopicSubscriptionOptionsDTO.PlanOption> classPlans =
                subscriptionPlanRepository.findByClassId(subject.getClassId()).stream()
                        .filter(SubscriptionPlan::getIsActive)
                        .map(p -> toPlanOption(p, "CLASS", subject.getClassId()))
                        .collect(Collectors.toList());

        return TopicSubscriptionOptionsDTO.builder()
                .topicId(topicId)
                .topicName(topic.getTopicName())
                .subjectId(subject.getSubjectId())
                .subjectName(subject.getSubjectName())
                .classId(cls.getClassId())
                .className(cls.getClassName())
                .topicPlans(topicPlans)
                .subjectPlans(subjectPlans)
                .classPlans(classPlans)
                .build();
    }

    /**
     * Subscribe the current student to a subject (grants access to all topics within it).
     */
    @Transactional
    public void subscribeToSubject(Long customerId, Long subjectId) {
        edSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found: " + subjectId));

        boolean alreadySubscribed = userSubjectSubscriptionRepository
                .existsByCustomerIdAndSubjectIdAndIsActiveTrue(customerId, subjectId);
        if (alreadySubscribed) {
            throw new RuntimeException("Already subscribed to this subject");
        }

        UserSubjectSubscription sub = new UserSubjectSubscription();
        sub.setCustomerId(customerId);
        sub.setSubjectId(subjectId);
        sub.setSubscribedAt(LocalDateTime.now());
        sub.setIsActive(true);
        userSubjectSubscriptionRepository.save(sub);
    }

    /**
     * Subscribe the current student to a specific topic.
     */
    @Transactional
    public void subscribeToTopic(Long customerId, Long topicId) {
        edTopicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found: " + topicId));

        boolean alreadySubscribed = userTopicSubscriptionRepository
                .existsByCustomerIdAndTopicIdAndIsActiveTrue(customerId, topicId);
        if (alreadySubscribed) {
            throw new RuntimeException("Already subscribed to this topic");
        }

        UserTopicSubscription sub = new UserTopicSubscription();
        sub.setCustomerId(customerId);
        sub.setTopicId(topicId);
        sub.setSubscribedAt(LocalDateTime.now());
        sub.setIsActive(true);
        userTopicSubscriptionRepository.save(sub);
    }

    private TopicSubscriptionOptionsDTO.PlanOption toPlanOption(
            SubscriptionPlan plan, String targetType, Long targetId) {
        return TopicSubscriptionOptionsDTO.PlanOption.builder()
                .subscriptionId(plan.getSubscriptionId())
                .planName(plan.getPlanName())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .durationDays(plan.getDurationDays())
                .freeDays(plan.getFreeDays())
                .gracePeriodDays(plan.getGracePeriodDays())
                .targetType(targetType)
                .targetId(targetId)
                .build();
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
