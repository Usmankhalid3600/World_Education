package com.worldedu.worldeducation.topic.repository;

import com.worldedu.worldeducation.topic.entity.UserTopicSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTopicSubscriptionRepository extends JpaRepository<UserTopicSubscription, Long> {
    
    List<UserTopicSubscription> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    List<UserTopicSubscription> findByCustomerId(Long customerId);
    
    Optional<UserTopicSubscription> findByCustomerIdAndTopicId(Long customerId, Long topicId);
    
    boolean existsByCustomerIdAndTopicIdAndIsActiveTrue(Long customerId, Long topicId);
}
