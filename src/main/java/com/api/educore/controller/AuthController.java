package com.api.educore.controller;

import com.api.educore.dto.AuthRequest;
import com.api.educore.dto.AuthResponse;
import com.api.educore.dto.ChangePasswordRequest;
import com.api.educore.dto.RegisterRequest;
import com.api.educore.dto.UpdateProfileRequest;
import com.api.educore.model.User;
import com.api.educore.model.UserRole;
import com.api.educore.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Palavra-passe alterada com sucesso"));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, String>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        authService.updateProfile(request);
        return ResponseEntity.ok(Map.of("message", "Perfil atualizado com sucesso"));
    }
}
