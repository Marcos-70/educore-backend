package com.api.educore.controller;

import com.api.educore.model.User;
import com.api.educore.model.UserPermission;
import com.api.educore.repository.UserPermissionRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final UserPermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserPermission>> getUserPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(permissionRepository.findByUserId(userId));
    }

    @GetMapping("/user/{userId}/modules")
    public ResponseEntity<Map<String, Boolean>> getUserModulePermissions(@PathVariable Long userId) {
        List<UserPermission> perms = permissionRepository.findByUserId(userId);
        Map<String, Boolean> result = new HashMap<>();
        String[] allModules = {"dashboard","students","enrollments","academic","teachers","classes","schedules","grades","attendance","finance","transport","library","documents","reports","schools","settings"};
        for (String mod : allModules) {
            boolean enabled = perms.stream().anyMatch(p -> p.getModuleId().equals(mod) && p.isEnabled());
            result.put(mod, enabled);
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<?> updateUserPermissions(@PathVariable Long userId, @RequestBody Map<String, Boolean> modulePermissions) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null || currentUser.getSchool() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario nao encontrado"));
        }

        permissionRepository.deleteByUserId(userId);

        User targetUser = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        for (Map.Entry<String, Boolean> entry : modulePermissions.entrySet()) {
            if (entry.getValue()) {
                UserPermission perm = UserPermission.builder()
                        .user(targetUser)
                        .moduleId(entry.getKey())
                        .enabled(true)
                        .build();
                permissionRepository.save(perm);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Permissoes atualizadas com sucesso"));
    }
}
