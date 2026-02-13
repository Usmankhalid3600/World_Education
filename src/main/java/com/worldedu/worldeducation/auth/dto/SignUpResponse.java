package com.worldedu.worldeducation.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for signup response (after code sent)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponse {
    
    private String message;
    private String email;
    private Integer codeValidityMinutes;
}
