package com.api.educore.controller;

import com.api.educore.dto.AssessmentDTO;
import com.api.educore.dto.GradeDTO;
import com.api.educore.dto.ReportCardDTO;
import com.api.educore.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/assessments")
    public ResponseEntity<List<AssessmentDTO>> findAssessments(
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam(required = false) Long trimesterId) {
        return ResponseEntity.ok(gradeService.findAssessments(classId, subjectId, trimesterId));
    }

    @PostMapping("/assessments")
    public ResponseEntity<AssessmentDTO> createAssessment(@RequestBody AssessmentDTO dto) {
        return ResponseEntity.ok(gradeService.createAssessment(dto));
    }

    @DeleteMapping("/assessments/{id}")
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        gradeService.deleteAssessment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assessments/{assessmentId}")
    public ResponseEntity<List<GradeDTO>> findGradesByAssessment(@PathVariable Long assessmentId) {
        return ResponseEntity.ok(gradeService.findGradesByAssessment(assessmentId));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<GradeDTO>> findGradesByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeService.findGradesByStudent(studentId));
    }

    @PostMapping
    public ResponseEntity<GradeDTO> saveGrade(@RequestBody GradeDTO dto) {
        return ResponseEntity.ok(gradeService.saveGrade(dto));
    }

    @PostMapping("/batch")
    public ResponseEntity<?> saveGrades(@RequestBody List<GradeDTO> dtos) {
        try {
            List<GradeDTO> saved = dtos.stream().map(gradeService::saveGrade).toList();
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report-card")
    public ResponseEntity<ReportCardDTO> getReportCard(
            @RequestParam Long studentId,
            @RequestParam Long classId,
            @RequestParam Long trimesterId) {
        return ResponseEntity.ok(gradeService.getReportCard(studentId, classId, trimesterId));
    }

    @GetMapping("/report-cards")
    public ResponseEntity<List<ReportCardDTO>> getReportCardsByClass(
            @RequestParam Long classId,
            @RequestParam Long trimesterId) {
        return ResponseEntity.ok(gradeService.getReportCardsByClass(classId, trimesterId));
    }
}
