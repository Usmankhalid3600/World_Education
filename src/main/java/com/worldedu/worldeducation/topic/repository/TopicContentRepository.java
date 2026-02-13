package com.worldedu.worldeducation.topic.repository;

import com.worldedu.worldeducation.topic.entity.TopicContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicContentRepository extends JpaRepository<TopicContent, Long> {
    
    List<TopicContent> findByTopicIdAndIsActiveTrue(Long topicId);
    
    List<TopicContent> findByTopicId(Long topicId);
}
