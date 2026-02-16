package com.worldedu.worldeducation.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicDTO {
    
    private Long topicId;
    private Long subjectId;
    private String topicName;
    private LocalDateTime publishDate;
    private Boolean isActive;
    private Boolean isOpted;
    
    // Subscription plan information (for unopted topics)
    private BigDecimal subscriptionPrice;
    private String currency;
    private Integer durationDays;
    private Integer freeDays;
    private Integer gracePeriodDays;
}
