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
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String role;
    private String position;
    private String phone;
    private String avatar;
    private String address;
    private String gender;
    private String sexo;
    private boolean active;
    private Long schoolId;
    private String schoolName;
}
