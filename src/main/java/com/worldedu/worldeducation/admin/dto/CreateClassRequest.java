package com.worldedu.worldeducation.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClassRequest {
    @NotBlank(message = "Class name is required")
    @Size(min = 2, max = 100, message = "Class name must be between 2 and 100 characters")
    private String className;
    
    @NotNull(message = "Class number is required")
    @Positive(message = "Class number must be positive")
    private Integer classNumber;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private Boolean isActive = true;
}
