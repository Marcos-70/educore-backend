package com.api.educore.controller;

import com.api.educore.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionService.getUserPermissions(userId));
    }

    @PutMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, String>> updateUserPermissions(
            @PathVariable Long userId, @RequestBody Map<String, Boolean> permissions) {
        permissionService.updateUserPermissions(userId, permissions);
        return ResponseEntity.ok(Map.of("message", "Permissoes atualizadas com sucesso"));
    }
}
