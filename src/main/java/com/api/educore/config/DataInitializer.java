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
    private final UserPermissionRepository userPermissionRepository;

    @Override
    public void run(String... args) {
        School school = getOrCreateSchool();

        List<User> usersWithoutSchool = userRepository.findBySchoolId(null);
        for (User u : usersWithoutSchool) {
            u.setSchool(school);
            userRepository.save(u);
        }

        if (userRepository.count() == 0) {
            User admin = createUser("Super Admin", "superadmin@mawa.com", "SuperAdmin123!", UserRole.SUPER_ADMIN, school);
            User adminDir = createUser("Admin Mawa", "admin.mawa@gmail.com", "Admin123!", UserRole.ADMIN, school);
            createUser("Director Geral", "director@mawa.com", "Director123!", UserRole.DIRECTOR, school);
            createUser("Director Pedagogico", "dir.pedagogico@mawa.com", "DirPedagogico123!", UserRole.DIRECTOR_PEDAGOGICO, school);
            createUser("Secretario Mawa", "sec.mawa@gmail.com", "Secretario123!", UserRole.SECRETARIO, school);
            createUser("Secretaria Pedagogica", "sec.pedagogica@mawa.com", "SecPedagogica123!", UserRole.SECRETARIA_PEDAGOGICA, school);
            createUser("Professor Mawa", "prof.mawa@gmail.com", "Professor123!", UserRole.PROFESSOR, school);
            createUser("Tesoureiro", "tesoureiro@mawa.com", "Tesoureiro123!", UserRole.TESOUREIRO, school);
            createUser("Bibliotecario", "bibliotecario@mawa.com", "Bibliotecario123!", UserRole.BIBLIOTECARIO, school);

            // Default permissions
            createDefaultPermissions(admin, new String[]{"schools", "dashboard"});
            createDefaultPermissions(adminDir, new String[]{"dashboard","students","enrollments","academic","teachers","classes","schedules","grades","attendance","finance","transport","library","documents","reports","settings"});
            createDefaultPermissions(findUser("director@mawa.com"), new String[]{"dashboard","students","enrollments","academic","teachers","classes","schedules","grades","attendance","reports"});
            createDefaultPermissions(findUser("dir.pedagogico@mawa.com"), new String[]{"dashboard","academic","teachers","classes","schedules","grades","attendance","reports"});
            createDefaultPermissions(findUser("sec.mawa@gmail.com"), new String[]{"dashboard","students","enrollments","teachers","classes","schedules","attendance","finance","transport","library","documents"});
            createDefaultPermissions(findUser("sec.pedagogica@mawa.com"), new String[]{"dashboard","students","enrollments","academic","teachers","classes","schedules","attendance","documents"});
            createDefaultPermissions(findUser("prof.mawa@gmail.com"), new String[]{"dashboard","classes","schedules","grades","attendance","library"});
            createDefaultPermissions(findUser("tesoureiro@mawa.com"), new String[]{"dashboard","finance"});
            createDefaultPermissions(findUser("bibliotecario@mawa.com"), new String[]{"dashboard","library"});

            log.info("Dados iniciais criados com sucesso");
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

    private User createUser(String name, String email, String password, UserRole role, School school) {
        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .school(school)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    private void createDefaultPermissions(User user, String[] modules) {
        if (user == null) return;
        for (String moduleId : modules) {
            UserPermission perm = UserPermission.builder()
                    .user(user)
                    .moduleId(moduleId)
                    .enabled(true)
                    .build();
            userPermissionRepository.save(perm);
        }
    }
}
