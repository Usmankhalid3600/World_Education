package com.worldedu.worldeducation.subject.repository;

import com.worldedu.worldeducation.subject.entity.UserSubjectSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubjectSubscriptionRepository extends JpaRepository<UserSubjectSubscription, Long> {
    
    List<UserSubjectSubscription> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    List<UserSubjectSubscription> findByCustomerId(Long customerId);
    
    Optional<UserSubjectSubscription> findByCustomerIdAndSubjectId(Long customerId, Long subjectId);
    
    boolean existsByCustomerIdAndSubjectIdAndIsActiveTrue(Long customerId, Long subjectId);
}
