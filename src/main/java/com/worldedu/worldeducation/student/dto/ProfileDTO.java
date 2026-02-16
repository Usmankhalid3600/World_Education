package com.worldedu.worldeducation.student.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private Long customerId;
    private String userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String mobileNo;
    private String country;
    private String state;
    private String city;
    private String address;
    private String userCategory;
}
