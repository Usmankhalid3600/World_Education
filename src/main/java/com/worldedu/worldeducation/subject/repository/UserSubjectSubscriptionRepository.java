package com.worldedu.worldeducation.subject.repository;

import com.worldedu.worldeducation.subject.entity.UserSubjectSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubjectSubscriptionRepository extends JpaRepository<UserSubjectSubscription, Long> {
    
    List<UserSubjectSubscription> findByCustomerIdAndIsActiveTrue(Long customerId);
    
    boolean existsByCustomerIdAndSubjectIdAndIsActiveTrue(Long customerId, Long subjectId);
}
