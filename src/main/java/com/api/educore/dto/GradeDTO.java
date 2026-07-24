package com.api.educore.dto;

import lombok.Data;

@Data
public class GradeDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long assessmentId;
    private String assessmentName;
    private Long subjectId;
    private String subjectName;
    private double score;
    private String observations;
    private Long gradedById;
}
