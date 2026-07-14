package com.api.educore.controller;

import com.api.educore.dto.SchoolSettingsDTO;
import com.api.educore.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SchoolSettingsDTO> get() {
        return ResponseEntity.ok(settingsService.get());
    }

    @PostMapping
    public ResponseEntity<SchoolSettingsDTO> save(@RequestBody SchoolSettingsDTO dto) {
        return ResponseEntity.ok(settingsService.save(dto));
    }
}
