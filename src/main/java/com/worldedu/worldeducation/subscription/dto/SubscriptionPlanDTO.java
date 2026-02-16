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
    private String targetName; // Class/Subject/Topic name
    private Integer durationDays;
    private BigDecimal price;
    private String currency;
    private Integer gracePeriodDays;
    private Integer freeDays;
    private Boolean isActive;
}
