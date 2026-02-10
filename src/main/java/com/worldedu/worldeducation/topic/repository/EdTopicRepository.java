package com.worldedu.worldeducation.topic.repository;

import com.worldedu.worldeducation.topic.entity.EdTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdTopicRepository extends JpaRepository<EdTopic, Long> {
    
    List<EdTopic> findBySubjectIdAndIsActiveTrue(Long subjectId);
    
    List<EdTopic> findBySubjectId(Long subjectId);
}
