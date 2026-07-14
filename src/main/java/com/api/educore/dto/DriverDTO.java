package com.api.educore.dto;

import com.api.educore.model.Gender;
import com.api.educore.model.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DriverDTO {
    private Long id;
    private String code;
    private String name;
    private Gender gender;
    private LocalDate birthDate;
    private String documentId;
    private String nif;
    private String phone;
    private String phone2;
    private String email;
    private String address;
    private String licenseNumber;
    private String licenseCategory;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiry;
    private Status status;
    private LocalDate admissionDate;
    private Long assignedBusId;
    private String assignedBusPlate;
    private Long assignedRouteId;
    private String assignedRouteName;
    private String notes;
    private String adminNotes;
}
