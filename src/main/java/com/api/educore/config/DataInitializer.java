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
            User superAdmin = createUser("Manuel", "António", "superadmin@mawa.com", "SuperAdmin123!", UserRole.SUPER_ADMIN, "Super Administrador", "923 100 001", "MASCULINO", school);
            User admin = createUser("Carlos", "Machado", "admin.mawa@gmail.com", "Admin123!", UserRole.ADMIN, "Administrador Geral", "923 100 002", "MASCULINO", school);
            User director = createUser("Fernanda", "Lopes", "director@mawa.com", "Director123!", UserRole.DIRECTOR, "Director Geral", "923 100 003", "FEMININO", school);
            User dirPed = createUser("Paulo", "Mendes", "dir.pedagogico@mawa.com", "DirPed123!", UserRole.DIRECTOR_PEDAGOGICO, "Director Pedagogico", "923 100 004", "MASCULINO", school);
            User sec = createUser("Ana", "Silva", "sec.mawa@gmail.com", "Secretario123!", UserRole.SECRETARIO, "Secretario Administrativo", "923 100 005", "FEMININO", school);
            User secPed = createUser("Maria", "Costa", "sec.pedagogica@mawa.com", "SecPed123!", UserRole.SECRETARIA_PEDAGOGICA, "Secretaria Pedagogica", "923 100 006", "FEMININO", school);
            User prof = createUser("Joao", "Santos", "prof.mawa@gmail.com", "Professor123!", UserRole.PROFESSOR, "Professor de Matematica", "923 100 007", "MASCULINO", school);
            User tesoureiro = createUser("Ricardo", "Almeida", "tesoureiro@mawa.com", "Tesoureiro123!", UserRole.TESOUREIRO, "Tesoureiro", "923 100 008", "MASCULINO", school);
            User biblio = createUser("Teresa", "Oliveira", "bibliotecario@mawa.com", "Bibliotec123!", UserRole.BIBLIOTECARIO, "Bibliotecaria", "923 100 009", "FEMININO", school);

            createDefaultPermissions(superAdmin, new String[]{"schools", "dashboard"});
            createDefaultPermissions(admin, new String[]{"dashboard","students","enrollments","academic","teachers","classes","schedules","grades","attendance","finance","transport","library","documents","reports","settings"});
            createDefaultPermissions(director, new String[]{"dashboard","students","enrollments","academic","teachers","classes","schedules","grades","attendance","reports"});
            createDefaultPermissions(dirPed, new String[]{"dashboard","academic","teachers","classes","schedules","grades","attendance","reports"});
            createDefaultPermissions(sec, new String[]{"dashboard","students","enrollments","teachers","classes","schedules","attendance","finance","transport","library","documents"});
            createDefaultPermissions(secPed, new String[]{"dashboard","students","enrollments","academic","teachers","classes","schedules","attendance","documents"});
            createDefaultPermissions(prof, new String[]{"dashboard","classes","schedules","grades","attendance","library"});
            createDefaultPermissions(tesoureiro, new String[]{"dashboard","finance"});
            createDefaultPermissions(biblio, new String[]{"dashboard","library"});

            log.info("Dados iniciais criados: 1 escola, 9 utilizadores com permissoes");
        }
    }

    private School getOrCreateSchool() {
        return schoolRepository.findByName("ACADEMIA MAWA")
                .orElseGet(() -> {
                    School s = School.builder()
                            .name("ACADEMIA MAWA")
                            .nif("541789236")
                            .address("Rua da Missao, 45")
                            .city("Luanda")
                            .country("Angola")
                            .email("info@academiamawa.edu.ao")
                            .phone("+244 923 456 789")
                            .website("www.academiamawa.edu.ao")
                            .motto("Educacao de excelencia")
                            .active(true)
                            .build();
                    return schoolRepository.save(s);
                });
    }

    private User createUser(String firstName, String lastName, String email, String password,
                            UserRole role, String position, String phone, String gender, School school) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(email.split("@")[0])
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .position(position)
                .phone(phone)
                .gender(gender)
                .school(school)
                .active(true)
                .build();
        return userRepository.save(user);
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
