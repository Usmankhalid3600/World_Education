package com.worldedu.worldeducation.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicSearchResultDTO {
    private Long topicId;
    private String topicName;
    private String topicDescription;
    private Long subjectId;
    private String subjectName;
    private Long classId;
    private String className;
    private Boolean isSubscribed;
}
