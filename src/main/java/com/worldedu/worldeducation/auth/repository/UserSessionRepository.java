package com.worldedu.worldeducation.auth.repository;

import com.worldedu.worldeducation.auth.entity.User;
import com.worldedu.worldeducation.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    List<UserSession> findByUserAndIsActive(User user, Boolean isActive);
    
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false WHERE us.user = :user AND us.isActive = true")
    void deactivateAllSessionsForUser(@Param("user") User user);
}
