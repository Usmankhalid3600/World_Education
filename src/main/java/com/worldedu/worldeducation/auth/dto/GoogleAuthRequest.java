package com.worldedu.worldeducation.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Google OAuth request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleAuthRequest {
    
    private String googleToken;  // Google ID token from frontend
    
    // Additional profile information (may come from Google)
    private String email;
    private String firstName;
    private String lastName;
    private String googleId;
    
    // Additional required fields for profile
    private String mobileNo;
    private String country;
    private String state;
    private String city;
    private String address;
}
