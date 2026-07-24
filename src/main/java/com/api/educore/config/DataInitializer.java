package com.api.educore.config;

import com.api.educore.model.*;
import com.api.educore.repository.*;
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
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionRepository userPermissionRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Base de dados já populada, ignorando inicialização");
            return;
        }

        log.info("A inicializar dados padrão do sistema...");

        // 1. Criar Super Admin SEM escola (gestor da plataforma)
        User superAdmin = createSuperAdmin();

        // 2. Criar escola padrão
        School school = getOrCreateSchool();

        // 3. Criar utilizadores da escola (todos com escola atribuída)
        User admin = createUser("Carlos", "Machado", "admin@educore.com", "Admin123!", UserRole.ADMIN, "Administrador Geral", "923 100 002", "MASCULINO", school);
        User director = createUser("Fernanda", "Lopes", "director@educore.com", "Director123!", UserRole.DIRECTOR, "Director Geral", "923 100 003", "FEMININO", school);
        User dirPed = createUser("Paulo", "Mendes", "dirped@educore.com", "DirPed123!", UserRole.DIRECTOR_PEDAGOGICO, "Director Pedagógico", "923 100 004", "MASCULINO", school);
        User sec = createUser("Ana", "Silva", "secretario@educore.com", "Secretario123!", UserRole.SECRETARIO, "Secretário Administrativo", "923 100 005", "FEMININO", school);
        User secPed = createUser("Maria", "Costa", "secped@educore.com", "SecPed123!", UserRole.SECRETARIA_PEDAGOGICA, "Secretária Pedagógica", "923 100 006", "FEMININO", school);
        User prof = createUser("João", "Santos", "professor@educore.com", "Professor123!", UserRole.PROFESSOR, "Professor de Matemática", "923 100 007", "MASCULINO", school);
        User tesoureiro = createUser("Ricardo", "Almeida", "tesoureiro@educore.com", "Tesoureiro123!", UserRole.TESOUREIRO, "Tesoureiro", "923 100 008", "MASCULINO", school);
        User biblio = createUser("Teresa", "Oliveira", "bibliotecario@educore.com", "Bibliotec123!", UserRole.BIBLIOTECARIO, "Bibliotecária", "923 100 009", "FEMININO", school);

        // 4. Permissões do Super Admin (acesso total à plataforma)
        createDefaultPermissions(superAdmin, new String[]{
            // Gestão de Escolas
            "VIEW_ESCOLA", "CREATE_ESCOLA", "EDIT_ESCOLA", "DELETE_ESCOLA",
            // Gestão de Utilizadores
            "VIEW_UTILIZADOR", "CREATE_UTILIZADOR", "EDIT_UTILIZADOR", "DELETE_UTILIZADOR",
            // Gestão de Permissões
            "VIEW_PERMISSAO", "EDIT_PERMISSAO",
            // Gestão Académica (acesso para suporte)
            "VIEW_ALUNO", "VIEW_PROFESSOR", "VIEW_TURMA", "VIEW_NOTA",
            "VIEW_ASSIDUIDADE", "VIEW_MATRICULA", "VIEW_FINANCEIRO",
            "VIEW_BIBLIOTECA", "VIEW_DOCUMENTO", "VIEW_TRANSPORTE",
            "VIEW_ACADEMICO", "VIEW_RELATORIO", "VIEW_CONFIGURACAO", "EDIT_CONFIGURACAO"
        });

        // 5. Permissões do Admin da escola
        createDefaultPermissions(admin, new String[]{
            "VIEW_ALUNO", "CREATE_ALUNO", "EDIT_ALUNO", "DELETE_ALUNO",
            "VIEW_PROFESSOR", "CREATE_PROFESSOR", "EDIT_PROFESSOR", "DELETE_PROFESSOR",
            "VIEW_TURMA", "CREATE_TURMA", "EDIT_TURMA", "DELETE_TURMA",
            "VIEW_NOTA", "EDIT_NOTA",
            "VIEW_ASSIDUIDADE", "EDIT_ASSIDUIDADE",
            "VIEW_MATRICULA", "CREATE_MATRICULA", "EDIT_MATRICULA", "DELETE_MATRICULA",
            "VIEW_FINANCEIRO", "CREATE_FINANCEIRO", "EDIT_FINANCEIRO", "DELETE_FINANCEIRO",
            "VIEW_BIBLIOTECA", "CREATE_BIBLIOTECA", "EDIT_BIBLIOTECA",
            "VIEW_DOCUMENTO", "CREATE_DOCUMENTO", "EDIT_DOCUMENTO",
            "VIEW_TRANSPORTE", "CREATE_TRANSPORTE", "EDIT_TRANSPORTE",
            "VIEW_ACADEMICO", "CREATE_ACADEMICO", "EDIT_ACADEMICO",
            "VIEW_RELATORIO", "VIEW_CONFIGURACAO", "EDIT_CONFIGURACAO",
            "VIEW_UTILIZADOR", "CREATE_UTILIZADOR", "EDIT_UTILIZADOR"
        });

        // 6. Permissões dos outros utilizadores
        createDefaultPermissions(director, new String[]{
            "VIEW_ALUNO", "CREATE_ALUNO", "EDIT_ALUNO",
            "VIEW_PROFESSOR", "CREATE_PROFESSOR", "EDIT_PROFESSOR",
            "VIEW_TURMA", "CREATE_TURMA", "EDIT_TURMA",
            "VIEW_NOTA", "EDIT_NOTA",
            "VIEW_ASSIDUIDADE", "EDIT_ASSIDUIDADE",
            "VIEW_MATRICULA", "CREATE_MATRICULA", "EDIT_MATRICULA",
            "VIEW_ACADEMICO", "CREATE_ACADEMICO", "EDIT_ACADEMICO",
            "VIEW_RELATORIO", "VIEW_CONFIGURACAO"
        });

        createDefaultPermissions(dirPed, new String[]{
            "VIEW_ACADEMICO", "CREATE_ACADEMICO", "EDIT_ACADEMICO",
            "VIEW_PROFESSOR", "CREATE_PROFESSOR",
            "VIEW_TURMA", "CREATE_TURMA", "EDIT_TURMA",
            "VIEW_NOTA", "EDIT_NOTA",
            "VIEW_ASSIDUIDADE", "EDIT_ASSIDUIDADE",
            "VIEW_RELATORIO"
        });

        createDefaultPermissions(sec, new String[]{
            "VIEW_ALUNO", "CREATE_ALUNO", "EDIT_ALUNO",
            "VIEW_PROFESSOR", "CREATE_PROFESSOR",
            "VIEW_TURMA", "CREATE_TURMA",
            "VIEW_ASSIDUIDADE", "EDIT_ASSIDUIDADE",
            "VIEW_FINANCEIRO", "CREATE_FINANCEIRO", "EDIT_FINANCEIRO",
            "VIEW_TRANSPORTE", "CREATE_TRANSPORTE",
            "VIEW_BIBLIOTECA",
            "VIEW_DOCUMENTO", "CREATE_DOCUMENTO"
        });

        createDefaultPermissions(secPed, new String[]{
            "VIEW_ALUNO", "CREATE_ALUNO", "EDIT_ALUNO",
            "VIEW_MATRICULA", "CREATE_MATRICULA",
            "VIEW_ACADEMICO",
            "VIEW_PROFESSOR",
            "VIEW_TURMA", "CREATE_TURMA",
            "VIEW_ASSIDUIDADE", "EDIT_ASSIDUIDADE",
            "VIEW_DOCUMENTO", "CREATE_DOCUMENTO"
        });

        createDefaultPermissions(prof, new String[]{
            "VIEW_TURMA", "EDIT_TURMA",
            "VIEW_NOTA", "EDIT_NOTA",
            "VIEW_ASSIDUIDADE", "EDIT_ASSIDUIDADE",
            "VIEW_BIBLIOTECA"
        });

        createDefaultPermissions(tesoureiro, new String[]{
            "VIEW_FINANCEIRO", "CREATE_FINANCEIRO", "EDIT_FINANCEIRO", "DELETE_FINANCEIRO"
        });

        createDefaultPermissions(biblio, new String[]{
            "VIEW_BIBLIOTECA", "CREATE_BIBLIOTECA", "EDIT_BIBLIOTECA", "DELETE_BIBLIOTECA"
        });

        log.info("=== Dados iniciais criados com sucesso ===");
        log.info("Super Admin: superadmin@educore.com / SuperAdmin123!");
        log.info("Admin Escola: admin@educore.com / Admin123!");
        log.info("Escola padrão: {}", school.getName());
    }

    /**
     * Criar Super Admin SEM escola associada.
     * O Super Admin é o gestor da plataforma e não pertence a nenhuma escola.
     */
    private User createSuperAdmin() {
        User superAdmin = User.builder()
                .firstName("Super")
                .lastName("Administrador")
                .username("superadmin")
                .email("superadmin@educore.com")
                .password(passwordEncoder.encode("SuperAdmin123!"))
                .role(UserRole.SUPER_ADMIN)
                .position("Super Administrador da Plataforma")
                .phone("923 100 001")
                .gender("MASCULINO")
                .school(null) // Super Admin NÃO pertence a nenhuma escola
                .active(true)
                .build();
        return userRepository.save(superAdmin);
    }

    private School getOrCreateSchool() {
        return schoolRepository.findByName("EduCore Academy")
                .orElseGet(() -> {
                    School s = School.builder()
                            .name("EduCore Academy")
                            .nif("541789236")
                            .address("Rua da Missão, 45")
                            .city("Luanda")
                            .country("Angola")
                            .email("info@educoreacademy.edu.ao")
                            .phone("+244 923 456 789")
                            .website("www.educoreacademy.edu.ao")
                            .motto("Educação de excelência")
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
                .school(school) // Utilizadores da escola têm escola associada
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private void createDefaultPermissions(User user, String[] permissionNames) {
        if (user == null) return;
        for (String permName : permissionNames) {
            try {
                Permission perm = Permission.valueOf(permName);
                UserPermission up = UserPermission.builder()
                        .user(user)
                        .permission(perm)
                        .enabled(true)
                        .build();
                userPermissionRepository.save(up);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
