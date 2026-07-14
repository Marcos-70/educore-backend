package com.api.educore.dto;

import com.api.educore.model.Gender;
import com.api.educore.model.ReaderType;
import com.api.educore.model.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LibraryReaderDTO {
    private Long id;
    private String code;
    private String name;
    private String email;
    private String phone;
    private ReaderType type;
    private Status status;
    private String className;
    private String documentId;
    private String address;
    private LocalDate birthDate;
    private Gender gender;
    private String profession;
    private String institution;
    private String guardianName;
    private String guardianPhone;
    private String notes;
    private int activeLoans;
}
