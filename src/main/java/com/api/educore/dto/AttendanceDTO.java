package com.api.educore.dto;

import com.api.educore.model.AttendanceStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long schoolClassId;
    private String schoolClassName;
    private LocalDate date;
    private AttendanceStatus status;
    private String reason;
    private Long recordedById;
}
