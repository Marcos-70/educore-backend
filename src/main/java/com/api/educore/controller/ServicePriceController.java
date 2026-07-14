package com.api.educore.controller;

import com.api.educore.dto.ServicePriceDTO;
import com.api.educore.service.ServicePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-prices")
@RequiredArgsConstructor
public class ServicePriceController {

    private final ServicePriceService service;

    @GetMapping
    public ResponseEntity<List<ServicePriceDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ServicePriceDTO>> findActive() {
        return ResponseEntity.ok(service.findActive());
    }

    @GetMapping("/by-category/{category}")
    public ResponseEntity<ServicePriceDTO> findByCategory(@PathVariable String category) {
        return ResponseEntity.ok(service.findByCategory(category));
    }

    @GetMapping("/list-by-category/{category}")
    public ResponseEntity<List<ServicePriceDTO>> listByCategory(@PathVariable String category) {
        return ResponseEntity.ok(service.listByCategory(category));
    }

    @GetMapping("/by-category/{category}/class/{classLevel}")
    public ResponseEntity<ServicePriceDTO> findByCategoryAndClass(
            @PathVariable String category, @PathVariable String classLevel) {
        return ResponseEntity.ok(service.findByCategoryAndClass(category, classLevel));
    }

    @PostMapping
    public ResponseEntity<ServicePriceDTO> create(@RequestBody ServicePriceDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicePriceDTO> update(@PathVariable Long id, @RequestBody ServicePriceDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
