package com.worldedu.worldeducation.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
