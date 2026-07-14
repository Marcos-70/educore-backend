package com.api.educore.controller;

import com.api.educore.dto.ClassDTO;
import com.api.educore.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public ResponseEntity<List<ClassDTO>> findAll() {
        return ResponseEntity.ok(classService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(classService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClassDTO> create(@RequestBody ClassDTO dto) {
        return ResponseEntity.ok(classService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassDTO> update(@PathVariable Long id, @RequestBody ClassDTO dto) {
        return ResponseEntity.ok(classService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        classService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
