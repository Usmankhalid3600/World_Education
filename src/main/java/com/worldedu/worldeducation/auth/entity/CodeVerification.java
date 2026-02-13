package com.worldedu.worldeducation.auth.entity;

import com.worldedu.worldeducation.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_verification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "secret_code", nullable = false)
    private String secretCode;

    @Column(name = "action", nullable = false)
    private String action;  // e.g., "SIGNUP", "PASSWORD_RESET", etc.

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(name = "generation_time", nullable = false)
    private LocalDateTime generationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VerificationStatus status = VerificationStatus.ACTIVE;

    @Column(name = "user_id")
    private String userId;  // Email or userId for verification

    @PrePersist
    protected void onCreate() {
        if (generationTime == null) {
            generationTime = LocalDateTime.now();
        }
    }
}
