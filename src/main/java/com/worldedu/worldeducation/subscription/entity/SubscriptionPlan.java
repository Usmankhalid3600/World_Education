package com.worldedu.worldeducation.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    // Explicit FK columns — exactly one is non-null, determined by target_type
    @Column(name = "class_id")
    private Long classId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 10)
    private String currency = "USD";

    @Column(name = "grace_period_days")
    private Integer gracePeriodDays;

    @Column(name = "free_days")
    private Integer freeDays;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the concrete target ID based on target_type.
     * CLASS → classId, SUBJECT → subjectId, TOPIC → topicId.
     */
    public Long getTargetId() {
        if (targetType == null) return null;
        return switch (targetType) {
            case CLASS -> classId;
            case SUBJECT -> subjectId;
            case TOPIC -> topicId;
        };
    }

    public enum TargetType {
        CLASS, SUBJECT, TOPIC
    }
}
