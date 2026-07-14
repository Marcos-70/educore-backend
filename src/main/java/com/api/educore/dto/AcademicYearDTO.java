package com.api.educore.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AcademicYearDTO {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private List<TrimesterDTO> trimesters;
}
