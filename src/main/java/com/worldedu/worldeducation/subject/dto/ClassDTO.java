package com.worldedu.worldeducation.subject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassDTO {
    
    private Long classId;
    private String className;
    private Integer classNumber;
    private Boolean isActive;
}
