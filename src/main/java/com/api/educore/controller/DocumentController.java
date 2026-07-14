package com.api.educore.controller;

import com.api.educore.dto.DocumentDTO;
import com.api.educore.model.DocumentStatus;
import com.api.educore.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<List<DocumentDTO>> findAll() {
        return ResponseEntity.ok(documentService.findAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<DocumentDTO>> findByStatus(@PathVariable DocumentStatus status) {
        return ResponseEntity.ok(documentService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<DocumentDTO> create(@RequestBody DocumentDTO dto) {
        return ResponseEntity.ok(documentService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> update(@PathVariable Long id, @RequestBody DocumentDTO dto) {
        return ResponseEntity.ok(documentService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DocumentDTO> updateStatus(@PathVariable Long id, @RequestParam DocumentStatus status) {
        return ResponseEntity.ok(documentService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
