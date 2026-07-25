package com.api.educore.dto;

import lombok.Data;

@Data
public class SubjectGradeDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private Long trimesterId;
    private String trimesterName;
    private Long classId;
    private double score;
    private String observations;
    private Long gradedById;
}
