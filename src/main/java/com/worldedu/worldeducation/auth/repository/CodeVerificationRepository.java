package com.worldedu.worldeducation.auth.repository;

import com.worldedu.worldeducation.auth.entity.CodeVerification;
import com.worldedu.worldeducation.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CodeVerificationRepository extends JpaRepository<CodeVerification, Long> {
    
    Optional<CodeVerification> findByUserIdAndSecretCodeAndStatusAndActionAndExpiryTimeAfter(
            String userId, 
            String secretCode, 
            VerificationStatus status, 
            String action, 
            LocalDateTime currentTime
    );
    
    Optional<CodeVerification> findTopByUserIdAndActionAndStatusOrderByGenerationTimeDesc(
            String userId,
            String action,
            VerificationStatus status
    );
    
    List<CodeVerification> findByUserIdAndAction(String userId, String action);
    
    List<CodeVerification> findByExpiryTimeBeforeAndStatus(LocalDateTime currentTime, VerificationStatus status);
}
