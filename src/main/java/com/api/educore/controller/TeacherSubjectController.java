package com.api.educore.controller;

import com.api.educore.dto.TeacherSubjectDTO;
import com.api.educore.service.TeacherSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-subjects")
@RequiredArgsConstructor
public class TeacherSubjectController {

    private final TeacherSubjectService teacherSubjectService;

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherSubjectDTO>> getByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(teacherSubjectService.getByTeacher(teacherId));
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<List<TeacherSubjectDTO>> getBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(teacherSubjectService.getBySubject(subjectId));
    }

    @PostMapping
    public ResponseEntity<TeacherSubjectDTO> add(@RequestBody Map<String, Long> body) {
        Long teacherId = body.get("teacherId");
        Long subjectId = body.get("subjectId");
        return ResponseEntity.ok(teacherSubjectService.add(teacherId, subjectId));
    }

    @DeleteMapping("/teacher/{teacherId}/subject/{subjectId}")
    public ResponseEntity<Map<String, String>> remove(
            @PathVariable Long teacherId,
            @PathVariable Long subjectId) {
        teacherSubjectService.remove(teacherId, subjectId);
        return ResponseEntity.ok(Map.of("message", "Disciplina removida do professor"));
    }
}
