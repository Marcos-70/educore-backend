package com.api.educore.controller;

import com.api.educore.model.School;
import com.api.educore.model.User;
import com.api.educore.repository.SchoolRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<School>> getAll() {
        return ResponseEntity.ok(schoolRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<School> getById(@PathVariable Long id) {
        return ResponseEntity.ok(schoolRepository.findById(id).orElseThrow(() -> new RuntimeException("Escola nao encontrada")));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<School> create(@RequestBody Map<String, Object> body) {
        School school = new School();
        applyFields(school, body);
        return ResponseEntity.ok(schoolRepository.save(school));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<School> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        School school = schoolRepository.findById(id).orElseThrow(() -> new RuntimeException("Escola nao encontrada"));
        applyFields(school, body);
        return ResponseEntity.ok(schoolRepository.save(school));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<School> toggleActive(@PathVariable Long id) {
        School school = schoolRepository.findById(id).orElseThrow(() -> new RuntimeException("Escola nao encontrada"));
        school.setActive(!school.isActive());
        return ResponseEntity.ok(schoolRepository.save(school));
    }

    private void applyFields(School school, Map<String, Object> body) {
        if (body.containsKey("name")) school.setName((String) body.get("name"));
        if (body.containsKey("nif")) school.setNif((String) body.get("nif"));
        if (body.containsKey("address")) school.setAddress((String) body.get("address"));
        if (body.containsKey("city")) school.setCity((String) body.get("city"));
        if (body.containsKey("country")) school.setCountry((String) body.get("country"));
        if (body.containsKey("email")) school.setEmail((String) body.get("email"));
        if (body.containsKey("phone")) school.setPhone((String) body.get("phone"));
        if (body.containsKey("website")) school.setWebsite((String) body.get("website"));
        if (body.containsKey("logo")) school.setLogo((String) body.get("logo"));
        if (body.containsKey("motto")) school.setMotto((String) body.get("motto"));
        if (body.containsKey("active")) school.setActive((Boolean) body.get("active"));
        if (body.containsKey("directorId") && body.get("directorId") != null) {
            Long directorId = Long.valueOf(body.get("directorId").toString());
            User director = userRepository.findById(directorId).orElse(null);
            school.setDirector(director);
        } else if (body.containsKey("directorId") && body.get("directorId") == null) {
            school.setDirector(null);
        }
    }
}
