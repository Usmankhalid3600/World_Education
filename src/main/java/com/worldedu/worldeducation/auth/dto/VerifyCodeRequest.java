package com.worldedu.worldeducation.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for verification code submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeRequest {
    
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Verification code is required")
    private String code;
}
