package com.worldedu.worldeducation.topic.service;

import com.worldedu.worldeducation.topic.dto.TopicDTO;
import com.worldedu.worldeducation.topic.dto.TopicListResponse;
import com.worldedu.worldeducation.topic.entity.EdTopic;
import com.worldedu.worldeducation.topic.entity.UserTopicSubscription;
import com.worldedu.worldeducation.topic.repository.EdTopicRepository;
import com.worldedu.worldeducation.topic.repository.UserTopicSubscriptionRepository;
import com.worldedu.worldeducation.subject.entity.EdSubject;
import com.worldedu.worldeducation.subject.repository.EdSubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
            
            TopicDTO topicDTO = TopicDTO.builder()
                    .topicId(topic.getTopicId())
                    .subjectId(topic.getSubjectId())
                    .topicName(topic.getTopicName())
                    .publishDate(topic.getPublishDate())
                    .isActive(topic.getIsActive())
                    .isOpted(isOpted)
                    .build();

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
}
