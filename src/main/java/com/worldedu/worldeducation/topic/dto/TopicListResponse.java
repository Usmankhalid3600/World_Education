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
public class TopicListResponse {
    
    private Long subjectId;
    private String subjectName;
    private List<TopicDTO> optedTopics;
    private List<TopicDTO> unoptedTopics;
    private Integer totalTopics;
    private Integer optedCount;
    private Integer unoptedCount;
}
