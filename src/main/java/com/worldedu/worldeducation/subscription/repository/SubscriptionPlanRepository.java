package com.worldedu.worldeducation.subscription.repository;

import com.worldedu.worldeducation.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    
    List<SubscriptionPlan> findByTargetTypeAndTargetId(SubscriptionPlan.TargetType targetType, Long targetId);
    
    Optional<SubscriptionPlan> findByTargetTypeAndTargetIdAndIsActiveTrue(
        SubscriptionPlan.TargetType targetType, Long targetId);
    
    List<SubscriptionPlan> findByTargetType(SubscriptionPlan.TargetType targetType);
    
    List<SubscriptionPlan> findByIsActiveTrue();
}
