package com.worldedu.worldeducation.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDTO {
    
    private Long subjectId;
    private Long classId;
    private String subjectName;
    private Boolean isActive;
    private Boolean isOpted;
}
