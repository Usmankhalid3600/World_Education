package com.worldedu.worldeducation.topic.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicContentDTO {
    
    private Long contentId;
    private Long topicId;
    private String fileName;
    private String filePathUrl;
    private String fileType;
    private String contentDataBase64;  // Base64 encoded binary data
    private Long contentSize;  // Size in bytes
    private Long uploadedBy;
    private LocalDateTime uploadedAt;
    private Boolean isActive;

    // Custom method to set binary data and auto-convert to Base64
    public void setTopicContentData(byte[] topicContentData) {
        if (topicContentData != null && topicContentData.length > 0) {
            this.contentDataBase64 = Base64.getEncoder().encodeToString(topicContentData);
            this.contentSize = (long) topicContentData.length;
        } else {
            this.contentDataBase64 = null;
            this.contentSize = 0L;
        }
    }
}
