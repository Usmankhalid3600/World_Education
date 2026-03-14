package com.worldedu.worldeducation.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "User ID is required")
    private String userId;
}
