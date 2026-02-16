package com.worldedu.worldeducation.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MySubscriptionDTO {
    private Long subscriptionId;
    private String type; // SUBJECT or TOPIC
    private Long targetId;
    private String targetName;
    private LocalDateTime subscribedAt;
    private LocalDateTime expiryDate;
    private Integer remainingDays;
    private Boolean isActive;
    private String status; // ACTIVE, EXPIRED, IN_GRACE_PERIOD
    private BigDecimal price;
    private String currency;
    private Integer durationDays;
}
