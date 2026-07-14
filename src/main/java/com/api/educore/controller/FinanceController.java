package com.api.educore.controller;

import com.api.educore.dto.PaymentDTO;
import com.api.educore.model.PaymentStatus;
import com.api.educore.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping
    public ResponseEntity<List<PaymentDTO>> findAll() {
        return ResponseEntity.ok(financeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.findById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentDTO>> findByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(financeService.findByStatus(status));
    }

    @PostMapping
    public ResponseEntity<PaymentDTO> create(@RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(financeService.create(dto));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentDTO> cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(financeService.cancel(id, body.get("reason"), body.get("observation")));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
                "totalCollected", financeService.getTotalCollected(),
                "totalPending", financeService.getTotalPending(),
                "paidCount", financeService.countPaid(),
                "unpaidCount", financeService.countUnpaid()
        ));
    }
}
