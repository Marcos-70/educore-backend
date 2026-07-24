package com.api.educore.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AssessmentDTO {
    private Long id;
    private String name;
    private String type;
    private Long schoolClassId;
    private String schoolClassName;
    private Long subjectId;
    private String subjectName;
    private double maxScore;
    private double weight;
    private LocalDate date;
    private Long trimesterId;
}
