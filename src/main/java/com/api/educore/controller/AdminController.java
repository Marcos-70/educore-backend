package com.api.educore.controller;

import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetData() {
        userRepository.deleteAll();
        schoolRepository.deleteAll();

        School school = School.builder()
                .name("ACADEMIA MAWA")
                .nif("541789236")
                .address("Luanda, Angola")
                .city("Luanda")
                .country("Angola")
                .email("info@academiamawa.edu.ao")
                .phone("+244 923 456 789")
                .motto("Educacao de excelencia")
                .active(true)
                .build();
        school = schoolRepository.save(school);

        createUser("Admin Mawa", "admin.mawa@gmail.com", "Admin123!", UserRole.ADMIN, school);
        createUser("Secretario Mawa", "sec.mawa@gmail.com", "Secretario123!", UserRole.SECRETARIO, school);
        createUser("Professor Mawa", "prof.mawa@gmail.com", "Professor123!", UserRole.PROFESSOR, school);
        createUser("Director Mawa", "dir.mawa@gmail.com", "Director123!", UserRole.DIRECTOR, school);

        return ResponseEntity.ok(Map.of("message", "Dados reiniciados com sucesso. Faca login com as credenciais do administrador."));
    }

    private void createUser(String name, String email, String password, UserRole role, School school) {
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .school(school)
                .active(true)
                .build();
        userRepository.save(user);
    }
}
