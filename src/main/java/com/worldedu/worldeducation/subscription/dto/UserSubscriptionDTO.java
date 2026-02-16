package com.worldedu.worldeducation.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionDTO {
    private Long subscriptionId;
    private Long customerId;
    private String userId;
    private String userName;
    private Long subjectId;
    private String subjectName;
    private Long topicId;
    private String topicName;
    private String subscribedAt;
    private Boolean isActive;
}
