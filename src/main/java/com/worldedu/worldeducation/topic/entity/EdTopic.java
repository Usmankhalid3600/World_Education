package com.worldedu.worldeducation.topic.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ed_topics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long topicId;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
