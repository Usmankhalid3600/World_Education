package com.worldedu.worldeducation.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTopicRequest {
    @NotNull(message = "Subject ID is required")
    @Positive(message = "Subject ID must be positive")
    private Long subjectId;
    
    @NotBlank(message = "Topic name is required")
    @Size(min = 2, max = 100, message = "Topic name must be between 2 and 100 characters")
    private String topicName;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private LocalDateTime publishDate;
    
    private Boolean isActive = true;
}
