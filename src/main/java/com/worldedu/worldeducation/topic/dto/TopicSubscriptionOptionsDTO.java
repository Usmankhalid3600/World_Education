package com.worldedu.worldeducation.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Returned when a student tries to access a topic they are not subscribed to.
 * Groups available subscription plans by level: topic, subject, and class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicSubscriptionOptionsDTO {

    private Long topicId;
    private String topicName;

    private Long subjectId;
    private String subjectName;

    private Long classId;
    private String className;

    /** Plans that grant access to this specific topic only. */
    private List<PlanOption> topicPlans;

    /** Plans that grant access to the entire parent subject (including this topic). */
    private List<PlanOption> subjectPlans;

    /** Plans that grant access to the entire class (all subjects and topics). */
    private List<PlanOption> classPlans;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanOption {
        private Long subscriptionId;
        private String planName;
        private BigDecimal price;
        private String currency;
        private Integer durationDays;
        private Integer freeDays;
        private Integer gracePeriodDays;
        /** TOPIC | SUBJECT | CLASS */
        private String targetType;
        private Long targetId;
    }
}
