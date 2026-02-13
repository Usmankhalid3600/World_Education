package com.worldedu.worldeducation.auth.dto;

import com.worldedu.worldeducation.enums.UserCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user signup request
 * Collects all required information for user and profile creation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    
    // User table fields
    @NotBlank(message = "User ID is required")
    @Size(min = 4, max = 50, message = "User ID must be between 4 and 50 characters")
    private String userId;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotNull(message = "User category is required")
    private UserCategory userCategory;
    
    // Profile table fields
    @NotBlank(message = "First name is required")
    private String firstName;
    
    private String middleName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    private String country;
    private String state;
    private String city;
    private String address;
    
    @NotBlank(message = "Mobile number is required")
    private String mobileNo;
}
