package com.api.educore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCardDTO {
    private Long studentId;
    private String studentName;
    private String className;
    private String academicYear;
    private String trimesterName;

    private List<SubjectGrades> subjects;
    private double overallAverage;
    private String classification;
    private boolean passed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectGrades {
        private Long subjectId;
        private String subjectName;
        private List<GradeEntry> grades;
        private double subjectAverage;
        private int assessmentCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GradeEntry {
        private Long assessmentId;
        private String assessmentName;
        private String assessmentType;
        private double score;
        private double maxScore;
        private double weight;
    }
}
