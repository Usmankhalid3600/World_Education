package com.worldedu.worldeducation.topic.repository;

import com.worldedu.worldeducation.topic.entity.UserTopicSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTopicSubscriptionRepository extends JpaRepository<UserTopicSubscription, Long> {
    
    List<UserTopicSubscription> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    boolean existsByCustomerIdAndTopicIdAndIsActiveTrue(Long customerId, Long topicId);
}
