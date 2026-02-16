package com.worldedu.worldeducation.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSubscriptionPlanDTO {
    private Long subscriptionId;
    private String planName;
    private String targetType; // CLASS, SUBJECT, TOPIC
    private Long targetId;
    private String targetName; // Name of the class/subject/topic
    private Integer durationDays;
    private BigDecimal price;
    private String currency;
    private Integer freeDays;
    private Integer gracePeriodDays;
    private Boolean isSubscribed; // Whether current user is already subscribed
}
