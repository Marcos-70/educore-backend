package com.api.educore.controller;

import com.api.educore.dto.BusDTO;
import com.api.educore.dto.DriverDTO;
import com.api.educore.dto.TransportRouteDTO;
import com.api.educore.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transport")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    // Routes
    @GetMapping("/routes")
    public ResponseEntity<List<TransportRouteDTO>> findAllRoutes() {
        return ResponseEntity.ok(transportService.findAllRoutes());
    }

    @PostMapping("/routes")
    public ResponseEntity<TransportRouteDTO> createRoute(@RequestBody TransportRouteDTO dto) {
        return ResponseEntity.ok(transportService.createRoute(dto));
    }

    @PutMapping("/routes/{id}")
    public ResponseEntity<TransportRouteDTO> updateRoute(@PathVariable Long id, @RequestBody TransportRouteDTO dto) {
        return ResponseEntity.ok(transportService.updateRoute(id, dto));
    }

    @DeleteMapping("/routes/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        transportService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // Buses
    @GetMapping("/buses")
    public ResponseEntity<List<BusDTO>> findAllBuses() {
        return ResponseEntity.ok(transportService.findAllBuses());
    }

    @PostMapping("/buses")
    public ResponseEntity<BusDTO> createBus(@RequestBody BusDTO dto) {
        return ResponseEntity.ok(transportService.createBus(dto));
    }

    @PutMapping("/buses/{id}")
    public ResponseEntity<BusDTO> updateBus(@PathVariable Long id, @RequestBody BusDTO dto) {
        return ResponseEntity.ok(transportService.updateBus(id, dto));
    }

    @DeleteMapping("/buses/{id}")
    public ResponseEntity<Void> deleteBus(@PathVariable Long id) {
        transportService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    // Drivers
    @GetMapping("/drivers")
    public ResponseEntity<List<DriverDTO>> findAllDrivers() {
        return ResponseEntity.ok(transportService.findAllDrivers());
    }

    @PostMapping("/drivers")
    public ResponseEntity<DriverDTO> createDriver(@RequestBody DriverDTO dto) {
        return ResponseEntity.ok(transportService.createDriver(dto));
    }

    @PutMapping("/drivers/{id}")
    public ResponseEntity<DriverDTO> updateDriver(@PathVariable Long id, @RequestBody DriverDTO dto) {
        return ResponseEntity.ok(transportService.updateDriver(id, dto));
    }

    @DeleteMapping("/drivers/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        transportService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
}
