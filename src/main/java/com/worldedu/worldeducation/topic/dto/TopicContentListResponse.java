package com.worldedu.worldeducation.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicContentListResponse {
    
    private Long topicId;
    private String topicName;
    private Long subjectId;
    private String subjectName;
    private Boolean hasAccess;
    private String accessType;  // "TOPIC_SUBSCRIPTION" or "SUBJECT_SUBSCRIPTION"
    private List<TopicContentDTO> contents;
    private Integer totalContents;
}
