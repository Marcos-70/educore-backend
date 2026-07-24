package com.api.educore.dto;

import com.api.educore.model.Gender;
import com.api.educore.model.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentDTO {
    private Long id;

    @NotBlank(message = "Nome proprio e obrigatorio")
    private String firstName;

    @NotBlank(message = "Apelido e obrigatorio")
    private String lastName;

    private String studentNumber;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String nationality;

    @NotBlank(message = "NIF e obrigatorio")
    private String nif;

    private String email;
    private String phone;
    private String address;
    private String photo;
    private String guardianName;
    private String guardianContact;
    private String guardianEmail;
    private String relationship;
    private String previousSchool;
    private String emergencyContact;
    private String emergencyPhone;
    private String allergies;
    private String medicalNotes;
    private Status status;
}
