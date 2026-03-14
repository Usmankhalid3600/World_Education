package com.worldedu.worldeducation.subscription.dto;

import com.worldedu.worldeducation.subscription.entity.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private Long subscriptionId;
    private String planName;
    private SubscriptionPlan.TargetType targetType;
    private Long targetId;
    private String targetName;      // Short name: e.g. "Mathematics"
    private String targetFullPath;  // Full hierarchy: e.g. "Grade 5 > Mathematics"
    // Context IDs needed by the frontend to pre-populate cascading dropdowns on edit
    private Long contextClassId;    // Class that owns the target (all types)
    private Long contextSubjectId;  // Subject that owns the topic (TOPIC type only)
    private Integer durationDays;
    private BigDecimal price;
    private String currency;
    private Integer gracePeriodDays;
    private Integer freeDays;
    private Boolean isActive;
}
