package com.api.educore.dto;

import com.api.educore.model.Gender;
import com.api.educore.model.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Gender gender;
    private LocalDate birthDate;
    private String bi;
    private LocalDate admissionDate;
    private Status status;
    private String qualification;
    private String formationArea;
    private String department;
    private String subject;
    private String address;
    private String bio;
    private String photo;
}
