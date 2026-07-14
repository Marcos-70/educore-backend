package com.api.educore.dto;

import com.api.educore.model.CalendarEventType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CalendarEventDTO {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private CalendarEventType type;
    private Long academicYearId;
    private String description;
}
