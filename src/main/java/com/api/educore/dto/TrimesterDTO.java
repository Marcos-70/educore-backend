package com.api.educore.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TrimesterDTO {
    private Long id;
    private String name;
    private int sequenceNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private Long academicYearId;
}
