package com.api.educore.service;

import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final UserRepository userRepository;
    private final UserPermissionRepository permissionRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    public Map<String, Object> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return Map.of();

        List<UserPermission> perms = permissionRepository.findByUserId(userId);
        Set<String> permissions = new HashSet<>();
        for (UserPermission p : perms) {
            if (p.isEnabled()) {
                permissions.add(p.getPermission().name());
            }
        }

        return Map.of(
            "userId", userId,
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "role", user.getRole().name(),
            "permissions", permissions
        );
    }

    public void updateUserPermissions(Long userId, Map<String, Boolean> modulePermissions) {
        permissionRepository.deleteByUserId(userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilizador nao encontrado"));

        for (Map.Entry<String, Boolean> entry : modulePermissions.entrySet()) {
            if (entry.getValue()) {
                try {
                    Permission perm = Permission.valueOf(entry.getKey());
                    UserPermission up = UserPermission.builder()
                            .user(user)
                            .permission(perm)
                            .enabled(true)
                            .build();
                    permissionRepository.save(up);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public boolean hasPermission(String moduleId) {
        User user = getCurrentUser();
        if (user == null) return false;
        if (user.getRole() == UserRole.SUPER_ADMIN) return true;

        List<UserPermission> perms = permissionRepository.findByUserId(user.getId());
        return perms.stream().anyMatch(p -> p.isEnabled() && p.getPermission().name().contains(moduleId.toUpperCase()));
    }
}
