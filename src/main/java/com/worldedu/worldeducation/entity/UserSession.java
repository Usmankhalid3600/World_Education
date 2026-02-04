package com.worldedu.worldeducation.entity;

import com.worldedu.worldeducation.enums.DeviceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User user;

    @Column(name = "device_id")
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private DeviceType deviceType;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (loginTime == null) {
            loginTime = LocalDateTime.now();
        }
        if (lastActivityAt == null) {
            lastActivityAt = LocalDateTime.now();
        }
    }
}
