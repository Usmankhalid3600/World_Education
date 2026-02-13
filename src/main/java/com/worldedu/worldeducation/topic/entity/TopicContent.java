package com.worldedu.worldeducation.topic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "topic_contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long contentId;

    @Column(name = "topic_id", nullable = false)
    private Long topicId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path_url")
    private String filePathUrl;

    @Column(name = "file_type")
    private String fileType;

    @Lob
    @Column(name = "topic_content_data", columnDefinition = "LONGBLOB")
    private byte[] topicContentData;

    @Column(name = "uploaded_by")
    private Long uploadedBy;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
