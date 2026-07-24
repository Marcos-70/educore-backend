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
    public ResponseEntity<SaftDTO> generateSaft(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ResponseEntity.ok(saftService.generateSaft(startDate, endDate));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadSaftXml(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        String xml = saftService.generateSaftXml(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setContentDispositionFormData("attachment",
                "SAFT_" + startDate.substring(0, 10) + "_" + endDate.substring(0, 10) + ".xml");
        headers.setContentLength(xml.getBytes(StandardCharsets.UTF_8).length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(xml.getBytes(StandardCharsets.UTF_8));
    }
}
