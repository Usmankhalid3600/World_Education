package com.worldedu.worldeducation.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyResetCodeRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Verification code is required")
    private String code;
}
