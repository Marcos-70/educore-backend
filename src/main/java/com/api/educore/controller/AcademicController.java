package com.api.educore.controller;

import com.api.educore.dto.*;
import com.api.educore.service.AcademicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    // Academic Years
    @GetMapping("/years")
    public ResponseEntity<List<AcademicYearDTO>> findAllYears() {
        return ResponseEntity.ok(academicService.findAllYears());
    }

    @PostMapping("/years")
    public ResponseEntity<AcademicYearDTO> createYear(@RequestBody AcademicYearDTO dto) {
        return ResponseEntity.ok(academicService.createYear(dto));
    }

    @PutMapping("/years/{id}")
    public ResponseEntity<AcademicYearDTO> updateYear(@PathVariable Long id, @RequestBody AcademicYearDTO dto) {
        return ResponseEntity.ok(academicService.updateYear(id, dto));
    }

    @PatchMapping("/years/{id}/activate")
    public ResponseEntity<Void> setActiveYear(@PathVariable Long id) {
        academicService.setActiveYear(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/years/{id}")
    public ResponseEntity<Void> deleteYear(@PathVariable Long id) {
        academicService.deleteYear(id);
        return ResponseEntity.noContent().build();
    }

    // Trimesters
    @GetMapping("/years/{yearId}/trimesters")
    public ResponseEntity<List<TrimesterDTO>> findTrimesters(@PathVariable Long yearId) {
        return ResponseEntity.ok(academicService.findTrimestersByYear(yearId));
    }

    @PostMapping("/trimesters")
    public ResponseEntity<TrimesterDTO> createTrimester(@RequestBody TrimesterDTO dto) {
        return ResponseEntity.ok(academicService.createTrimester(dto));
    }

    @DeleteMapping("/trimesters/{id}")
    public ResponseEntity<Void> deleteTrimester(@PathVariable Long id) {
        academicService.deleteTrimester(id);
        return ResponseEntity.noContent().build();
    }

    // Courses
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> findAllCourses() {
        return ResponseEntity.ok(academicService.findAllCourses());
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseDTO> createCourse(@RequestBody CourseDTO dto) {
        return ResponseEntity.ok(academicService.createCourse(dto));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody CourseDTO dto) {
        return ResponseEntity.ok(academicService.updateCourse(id, dto));
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        academicService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // Subjects
    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectDTO>> findAllSubjects() {
        return ResponseEntity.ok(academicService.findAllSubjects());
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectDTO> createSubject(@RequestBody SubjectDTO dto) {
        return ResponseEntity.ok(academicService.createSubject(dto));
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<SubjectDTO> updateSubject(@PathVariable Long id, @RequestBody SubjectDTO dto) {
        return ResponseEntity.ok(academicService.updateSubject(id, dto));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        academicService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }

    // Calendar Events
    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventDTO>> findAllEvents() {
        return ResponseEntity.ok(academicService.findAllEvents());
    }

    @PostMapping("/events")
    public ResponseEntity<CalendarEventDTO> createEvent(@RequestBody CalendarEventDTO dto) {
        return ResponseEntity.ok(academicService.createEvent(dto));
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<CalendarEventDTO> updateEvent(@PathVariable Long id, @RequestBody CalendarEventDTO dto) {
        return ResponseEntity.ok(academicService.updateEvent(id, dto));
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        academicService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
