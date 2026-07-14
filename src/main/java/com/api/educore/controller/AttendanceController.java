package com.api.educore.controller;

import com.api.educore.dto.AttendanceDTO;
import com.api.educore.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<List<AttendanceDTO>> findByClassAndDate(
            @RequestParam Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.findByClassAndDate(classId, date));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceDTO>> findByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.findByStudent(studentId));
    }

    @PostMapping
    public ResponseEntity<Void> saveAttendance(@RequestBody List<AttendanceDTO> dtos) {
        attendanceService.saveAttendance(dtos);
        return ResponseEntity.ok().build();
    }
}
