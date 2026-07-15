package com.api.educore.config;

import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        School school = getOrCreateSchool();

        List<User> usersWithoutSchool = userRepository.findBySchoolId(null);
        if (!usersWithoutSchool.isEmpty()) {
            for (User user : usersWithoutSchool) {
                user.setSchool(school);
                userRepository.save(user);
            }
            log.info("Associados {} usuarios existentes a escola {}", usersWithoutSchool.size(), school.getName());
        }

        if (userRepository.count() == 0) {
            createDefaultUser("Admin Mawa", "admin.mawa@gmail.com", "admin123", UserRole.ADMIN, school);
            createDefaultUser("Secretario Mawa", "sec.mawa@gmail.com", "secretario123", UserRole.SECRETARIO, school);
            createDefaultUser("Professor Mawa", "prof.mawa@gmail.com", "professor123", UserRole.PROFESSOR, school);
            createDefaultUser("Director Mawa", "dir.mawa@gmail.com", "director123", UserRole.DIRECTOR, school);
            log.info("Utilizadores padrao criados com sucesso");
        }
    }

    private School getOrCreateSchool() {
        List<School> schools = schoolRepository.findAll();
        if (!schools.isEmpty()) {
            return schools.get(0);
        }
        School school = School.builder()
                .name("ACADEMIA MAWA")
                .nif("541789236")
                .address("Luanda, Angola")
                .email("info@academiamawa.edu.ao")
                .phone("+244 923 456 789")
                .active(true)
                .build();
        return schoolRepository.save(school);
    }

    private void createDefaultUser(String name, String email, String password, UserRole role, School school) {
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
