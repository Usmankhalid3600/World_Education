package com.worldedu.worldeducation.auth.dto;

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
public class LoginResponse {

    private Long customerId;
    private String userId;
    private UserCategory userCategory;
    private String firstName;
    private String lastName;
    private String email;
    private Long sessionId;
    private LocalDateTime loginTime;
    private String token;
    private String message;
}
