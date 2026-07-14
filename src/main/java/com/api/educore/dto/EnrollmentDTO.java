package com.api.educore.dto;

import com.api.educore.model.EnrollmentStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnrollmentDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private String academicYear;
    private EnrollmentStatus status;
    private String enrollmentType;
    private LocalDate enrollmentDate;
    private String photo;
    private String previousSchool;
}
