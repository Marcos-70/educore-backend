package com.api.educore.controller;

import com.api.educore.model.School;
import com.api.educore.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolRepository schoolRepository;

    @GetMapping
    public ResponseEntity<List<School>> getAll() {
        return ResponseEntity.ok(schoolRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<School> create(@RequestBody School school) {
        return ResponseEntity.ok(schoolRepository.save(school));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<School> update(@PathVariable Long id, @RequestBody School updated) {
        School school = schoolRepository.findById(id).orElseThrow(() -> new RuntimeException("Escola nao encontrada"));
        school.setName(updated.getName());
        school.setNif(updated.getNif());
        school.setAddress(updated.getAddress());
        school.setEmail(updated.getEmail());
        school.setPhone(updated.getPhone());
        school.setLogo(updated.getLogo());
        school.setActive(updated.isActive());
        return ResponseEntity.ok(schoolRepository.save(school));
    }
}
