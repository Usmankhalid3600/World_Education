package com.worldedu.worldeducation.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {

    /** Masked email address shown to user (e.g. "j***n@g***.com") */
    private String maskedEmail;

    /** How many minutes the reset code is valid */
    private int codeValidityMinutes;
}
