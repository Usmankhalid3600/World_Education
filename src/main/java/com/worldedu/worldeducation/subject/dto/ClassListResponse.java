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
public class ClassListResponse {
    
    private List<ClassDTO> classes;
    private Integer totalClasses;
}
