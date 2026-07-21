package com.api.educore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String address;
    private String city;
    private String country;
    private String biNumber;
    private String dateOfBirth;
    private String gender;
    private boolean active;
    private Long schoolId;
    private String schoolName;
}
