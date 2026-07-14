package com.api.educore.controller;

import com.api.educore.dto.EnrollmentDTO;
import com.api.educore.model.EnrollmentStatus;
import com.api.educore.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping
    public ResponseEntity<List<EnrollmentDTO>> findAll() {
        return ResponseEntity.ok(enrollmentService.findAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<EnrollmentDTO>> findByStatus(@PathVariable EnrollmentStatus status) {
        return ResponseEntity.ok(enrollmentService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<EnrollmentDTO> create(@RequestBody EnrollmentDTO dto) {
        return ResponseEntity.ok(enrollmentService.create(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EnrollmentDTO> updateStatus(@PathVariable Long id, @RequestParam EnrollmentStatus status) {
        return ResponseEntity.ok(enrollmentService.updateStatus(id, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnrollmentDTO> update(@PathVariable Long id, @RequestBody EnrollmentDTO dto) {
        return ResponseEntity.ok(enrollmentService.update(id, dto));
    }

    @GetMapping("/check/{studentId}/{academicYear}")
    public ResponseEntity<Boolean> isStudentEnrolled(@PathVariable Long studentId, @PathVariable String academicYear) {
        return ResponseEntity.ok(enrollmentService.isStudentEnrolledInYear(studentId, academicYear));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        enrollmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
