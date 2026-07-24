package com.api.educore.controller;

import com.api.educore.dto.SaftDTO;
import com.api.educore.service.SaftService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/saft")
@RequiredArgsConstructor
public class SaftController {

    private final SaftService saftService;

    @GetMapping("/generate")
    public ResponseEntity<SaftDTO> generateSaft(@RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(saftService.generateSaft(year));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadSaftXml(@RequestParam(defaultValue = "2026") int year) {
        String xml = saftService.generateSaftXml(year);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDispositionFormData("attachment",
                "SAFT_" + year + ".xml");
        headers.setContentLength(xml.getBytes(StandardCharsets.UTF_8).length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(xml.getBytes(StandardCharsets.UTF_8));
    }
}
