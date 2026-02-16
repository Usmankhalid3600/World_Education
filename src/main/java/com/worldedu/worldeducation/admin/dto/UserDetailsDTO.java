package com.worldedu.worldeducation.admin.dto;

import com.worldedu.worldeducation.enums.UserCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailsDTO {
    private Long customerId;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNo;
    private UserCategory userCategory;
    private Boolean accountLocked;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private Integer totalOptedSubjects;
    private Integer totalOptedTopics;
}
