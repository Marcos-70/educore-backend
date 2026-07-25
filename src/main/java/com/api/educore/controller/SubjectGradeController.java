package com.api.educore.controller;

import com.api.educore.dto.ReportCardDTO;
import com.api.educore.dto.SubjectGradeDTO;
import com.api.educore.service.SubjectGradeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subject-grades")
@RequiredArgsConstructor
public class SubjectGradeController {

    private static final Logger log = LoggerFactory.getLogger(SubjectGradeController.class);
    private final SubjectGradeService subjectGradeService;

    @GetMapping
    public ResponseEntity<?> listGrades(
            @RequestParam Long classId,
            @RequestParam Long subjectId,
            @RequestParam Long trimesterId) {
        try {
            return ResponseEntity.ok(subjectGradeService.listGrades(classId, subjectId, trimesterId));
        } catch (Exception e) {
            log.error("Error listing subject grades: classId={}, subjectId={}, trimesterId={}", classId, subjectId, trimesterId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<?> saveBatch(@RequestBody List<SubjectGradeDTO> dtos) {
        try {
            List<SubjectGradeDTO> saved = subjectGradeService.saveBatch(dtos);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error("Error saving subject grades batch", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    @GetMapping("/report-cards")
    public ResponseEntity<?> getReportCards(
            @RequestParam Long classId,
            @RequestParam Long trimesterId) {
        try {
            return ResponseEntity.ok(subjectGradeService.getReportCards(classId, trimesterId));
        } catch (Exception e) {
            log.error("Error getting report cards: classId={}, trimesterId={}", classId, trimesterId, e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }
}
