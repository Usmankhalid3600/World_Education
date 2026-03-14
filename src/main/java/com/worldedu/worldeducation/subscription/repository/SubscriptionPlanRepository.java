package com.worldedu.worldeducation.subscription.repository;

import com.worldedu.worldeducation.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    // Typed queries — one per entity type for real FK integrity
    List<SubscriptionPlan> findByClassId(Long classId);
    List<SubscriptionPlan> findBySubjectId(Long subjectId);
    List<SubscriptionPlan> findByTopicId(Long topicId);

    Optional<SubscriptionPlan> findByClassIdAndIsActiveTrue(Long classId);
    Optional<SubscriptionPlan> findBySubjectIdAndIsActiveTrue(Long subjectId);
    Optional<SubscriptionPlan> findByTopicIdAndIsActiveTrue(Long topicId);

    List<SubscriptionPlan> findByTargetType(SubscriptionPlan.TargetType targetType);

    List<SubscriptionPlan> findByIsActiveTrue();
}
