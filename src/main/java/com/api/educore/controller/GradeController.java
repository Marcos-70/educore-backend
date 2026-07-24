package com.api.educore.controller;

import com.api.educore.dto.AssessmentDTO;
import com.api.educore.dto.GradeDTO;
import com.api.educore.dto.ReportCardDTO;
import com.api.educore.service.GradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Slf4j
public class GradeController {

    private final GradeService gradeService;

    // Avaliacoes - independentes da disciplina
    @GetMapping("/assessments")
    public ResponseEntity<List<AssessmentDTO>> findAssessments(@RequestParam Long classId) {
        return ResponseEntity.ok(gradeService.findAssessments(classId));
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

    // Notas de uma avaliacao + disciplina
    @GetMapping("/assessments/{assessmentId}/subjects/{subjectId}")
    public ResponseEntity<List<GradeDTO>> findGradesByAssessmentAndSubject(
            @PathVariable Long assessmentId, @PathVariable Long subjectId) {
        return ResponseEntity.ok(gradeService.findGradesByAssessmentAndSubject(assessmentId, subjectId));
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
    public ResponseEntity<List<GradeDTO>> saveGrades(@RequestBody List<GradeDTO> dtos) {
        List<GradeDTO> saved = new java.util.ArrayList<>();
        for (GradeDTO dto : dtos) {
            try {
                saved.add(gradeService.saveGrade(dto));
            } catch (Exception e) {
                log.warn("Erro ao guardar nota do aluno {}: {}", dto.getStudentId(), e.getMessage());
            }
        }
        return ResponseEntity.ok(saved);
    }

    // Boletim de notas
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
