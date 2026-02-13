package com.worldedu.worldeducation.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for uploading topic content with file data
 * Used in multipart/form-data requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicContentUploadRequest {
    
    private Long topicId;
    private String fileName;
    private String fileType;
    // fileData will be received as MultipartFile in the controller
    
    // Optional: If you want to support Base64 upload via JSON
    private String fileDataBase64;
}
