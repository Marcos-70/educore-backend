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
        for (User user : usersWithoutSchool) {
            user.setSchool(school);
            userRepository.save(user);
        }

        if (userRepository.count() == 0) {
            createUser("Admin Mawa", "admin.mawa@gmail.com", "Admin123!", UserRole.ADMIN, school);
            createUser("Secretario Mawa", "sec.mawa@gmail.com", "Secretario123!", UserRole.SECRETARIO, school);
            createUser("Professor Mawa", "prof.mawa@gmail.com", "Professor123!", UserRole.PROFESSOR, school);
            createUser("Director Mawa", "dir.mawa@gmail.com", "Director123!", UserRole.DIRECTOR, school);
            log.info("Dados iniciais criados");
        }
    }

    private School getOrCreateSchool() {
        return schoolRepository.findByName("ACADEMIA MAWA")
                .orElseGet(() -> {
                    School s = School.builder()
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
                    return schoolRepository.save(s);
                });
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
