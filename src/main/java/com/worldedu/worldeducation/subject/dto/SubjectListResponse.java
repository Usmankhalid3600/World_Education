package com.worldedu.worldeducation.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectListResponse {
    
    private Long classId;
    private String className;
    private List<SubjectDTO> optedSubjects;
    private List<SubjectDTO> unoptedSubjects;
    private Integer totalSubjects;
    private Integer optedCount;
    private Integer unoptedCount;
}
