package com.api.educore.controller;

import com.api.educore.dto.ScheduleDTO;
import com.api.educore.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> findAll() {
        return ResponseEntity.ok(scheduleService.findAll());
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<ScheduleDTO>> findByClass(@PathVariable Long classId) {
        return ResponseEntity.ok(scheduleService.findByClass(classId));
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ScheduleDTO>> findByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(scheduleService.findByTeacher(teacherId));
    }

    @PostMapping
    public ResponseEntity<ScheduleDTO> create(@RequestBody ScheduleDTO dto) {
        return ResponseEntity.ok(scheduleService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
