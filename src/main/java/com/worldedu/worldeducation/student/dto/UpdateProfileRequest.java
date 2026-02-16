package com.worldedu.worldeducation.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String country;
    private String state;
    private String city;
    private String address;
}
