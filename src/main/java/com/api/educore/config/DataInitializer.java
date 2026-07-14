package com.api.educore.config;

import com.api.educore.model.User;
import com.api.educore.model.UserRole;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            createDefaultUser("Admin Mawa", "admin.mawa@gmail.com", "admin123", UserRole.ADMIN);
            createDefaultUser("Secretario Mawa", "sec.mawa@gmail.com", "secretario123", UserRole.SECRETARIO);
            createDefaultUser("Professor Mawa", "prof.mawa@gmail.com", "professor123", UserRole.PROFESSOR);
            createDefaultUser("Director Mawa", "dir.mawa@gmail.com", "director123", UserRole.DIRECTOR);
            log.info("Utilizadores padrao criados com sucesso");
            log.info("Credenciais: admin.mawa@gmail.com / admin123 (ADMIN)");
        }
    }

    private void createDefaultUser(String name, String email, String password, UserRole role) {
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(true)
                .build();
        userRepository.save(user);
    }
}
