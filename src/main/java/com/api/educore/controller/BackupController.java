package com.api.educore.controller;

import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AssessmentRepository assessmentRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final DocumentRepository documentRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final LibraryReaderRepository libraryReaderRepository;
    private final LibraryLoanRepository libraryLoanRepository;
    private final ServicePriceRepository servicePriceRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/sql")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadSqlBackup() {
        User user = getCurrentUser();
        if (user == null || user.getSchool() == null) {
            return ResponseEntity.badRequest().build();
        }
        Long schoolId = user.getSchool().getId();
        School school = user.getSchool();

        StringBuilder sql = new StringBuilder();
        sql.append("-- ========================================\n");
        sql.append("-- Backup EduCore - Sistema de Gestao Escolar\n");
        sql.append("-- Escola: ").append(school.getName()).append("\n");
        sql.append("-- Data: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sql.append("-- ========================================\n\n");

        sql.append("-- Escola\n");
        sql.append(String.format("INSERT INTO schools (id, name, nif, address, city, country, email, phone, website, logo, motto, active, director_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', NULL, '%s', %s, %s) ON CONFLICT (id) DO NOTHING;\n\n",
                school.getId(), esc(school.getName()), esc(school.getNif()), esc(school.getAddress()),
                esc(school.getCity()), esc(school.getCountry()), esc(school.getEmail()),
                esc(school.getPhone()), esc(school.getWebsite()), esc(school.getMotto()),
                school.isActive(), school.getDirector() != null ? school.getDirector().getId() : "NULL"));

        sql.append("-- Usuarios\n");
        List<User> users = userRepository.findBySchoolId(schoolId);
        for (User u : users) {
            sql.append(String.format("INSERT INTO users (id, name, email, password, role, phone, avatar, address, city, country, bi_number, date_of_birth, gender, active, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', NULL, '%s', '%s', '%s', '%s', '%s', '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    u.getId(), esc(u.getName()), esc(u.getEmail()), esc(u.getPassword()),
                    u.getRole() != null ? u.getRole().name() : "SECRETARIO",
                    esc(u.getPhone()), esc(u.getAddress()), esc(u.getCity()), esc(u.getCountry()),
                    esc(u.getBiNumber()), esc(u.getDateOfBirth()), esc(u.getGender()),
                    u.isActive(), schoolId));
        }
        sql.append("\n");

        sql.append("-- Configuracoes da Escola\n");
        SchoolSettings settings = schoolSettingsRepository.findBySchoolId(schoolId).orElse(null);
        if (settings != null) {
            sql.append(String.format("INSERT INTO school_settings (id, school_name, school_motto, nif, address, email, phone, currency, timezone, logo_path, active_academic_year, auto_billing, auto_backup, email_notifications, sms_notifications, push_notifications, language, date_format, currency_format, theme, compact_mode, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, %s, %s, %s, %s, '%s', '%s', '%s', '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    settings.getId(), esc(settings.getSchoolName()), esc(settings.getSchoolMotto()), esc(settings.getNif()),
                    esc(settings.getAddress()), esc(settings.getEmail()), esc(settings.getPhone()),
                    esc(settings.getCurrency()), esc(settings.getTimezone()), esc(settings.getLogoPath()),
                    esc(settings.getActiveAcademicYear()), settings.isAutoBilling(), settings.isAutoBackup(),
                    settings.isEmailNotifications(), settings.isSmsNotifications(), settings.isPushNotifications(),
                    esc(settings.getLanguage()), esc(settings.getDateFormat()), esc(settings.getCurrencyFormat()),
                    esc(settings.getTheme()), settings.isCompactMode(), schoolId));
        }
        sql.append("\n");

        sql.append("-- Alunos\n");
        List<Student> students = studentRepository.findBySchoolId(schoolId);
        for (Student s : students) {
            sql.append(String.format("INSERT INTO students (id, first_name, last_name, student_number, date_of_birth, gender, nationality, nif, email, phone, address, photo, guardian_name, guardian_contact, guardian_email, relationship, previous_school, emergency_contact, emergency_phone, allergies, medical_notes, status, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', NULL, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    s.getId(), esc(s.getFirstName()), esc(s.getLastName()), esc(s.getStudentNumber()),
                    s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "NULL",
                    s.getGender() != null ? s.getGender().name() : "NULL",
                    esc(s.getNationality()), esc(s.getNif()), esc(s.getEmail()),
                    esc(s.getPhone()), esc(s.getAddress()), esc(s.getGuardianName()),
                    esc(s.getGuardianContact()), esc(s.getGuardianEmail()), esc(s.getRelationship()),
                    esc(s.getPreviousSchool()), esc(s.getEmergencyContact()), esc(s.getEmergencyPhone()),
                    esc(s.getAllergies()), esc(s.getMedicalNotes()),
                    s.getStatus() != null ? s.getStatus().name() : "ACTIVE", schoolId));
        }
        sql.append("\n");

        sql.append("-- Professores\n");
        List<Teacher> teachers = teacherRepository.findBySchoolId(schoolId);
        for (Teacher t : teachers) {
            sql.append(String.format("INSERT INTO teachers (id, name, email, phone, gender, birth_date, bi, admission_date, status, qualification, formation_area, department, subject, address, bio, photo, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', NULL, %d) ON CONFLICT (id) DO NOTHING;\n",
                    t.getId(), esc(t.getName()), esc(t.getEmail()), esc(t.getPhone()),
                    t.getGender() != null ? t.getGender().name() : "NULL",
                    t.getBirthDate() != null ? t.getBirthDate().toString() : "NULL",
                    esc(t.getBi()),
                    t.getAdmissionDate() != null ? t.getAdmissionDate().toString() : "NULL",
                    t.getStatus() != null ? t.getStatus().name() : "ACTIVE",
                    esc(t.getQualification()), esc(t.getFormationArea()),
                    esc(t.getDepartment()), esc(t.getSubject()), esc(t.getAddress()), esc(t.getBio()),
                    schoolId));
        }
        sql.append("\n");

        sql.append("-- Turmas\n");
        List<SchoolClass> classes = schoolClassRepository.findBySchoolId(schoolId);
        for (SchoolClass c : classes) {
            sql.append(String.format("INSERT INTO school_classes (id, name, class_level, course, room, capacity, shift, academic_year, school_id) VALUES (%d, '%s', '%s', '%s', '%s', %d, '%s', '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    c.getId(), esc(c.getName()),
                    c.getClassLevel() != null ? c.getClassLevel().name() : "NULL",
                    esc(c.getCourse()), esc(c.getRoom()), c.getCapacity(),
                    c.getShift() != null ? c.getShift().name() : "NULL",
                    esc(c.getAcademicYear()), schoolId));
        }
        sql.append("\n");

        sql.append("-- Disciplinas\n");
        List<Subject> subjects = subjectRepository.findBySchoolId(schoolId);
        for (Subject s : subjects) {
            sql.append(String.format("INSERT INTO subjects (id, code, name, abbrev_name, description, class_level, type, minimum_grade, minimum_attendance, active, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %.1f, %d, %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    s.getId(), esc(s.getCode()), esc(s.getName()), esc(s.getAbbrevName()),
                    esc(s.getDescription()),
                    s.getClassLevel() != null ? s.getClassLevel().name() : "NULL",
                    s.getType() != null ? s.getType().name() : "NULL",
                    s.getMinimumGrade(), s.getMinimumAttendance(), s.isActive(), schoolId));
        }
        sql.append("\n");

        sql.append("-- Matriculas\n");
        List<Enrollment> enrollments = enrollmentRepository.findBySchoolId(schoolId);
        for (Enrollment e : enrollments) {
            sql.append(String.format("INSERT INTO enrollments (id, student_id, class_id, academic_year, status, enrollment_type, enrollment_date, photo, previous_school, school_id) VALUES (%d, %d, %d, '%s', '%s', '%s', '%s', NULL, '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    e.getId(),
                    e.getStudent() != null ? e.getStudent().getId() : 0,
                    e.getSchoolClass() != null ? e.getSchoolClass().getId() : 0,
                    esc(e.getAcademicYear()),
                    e.getStatus() != null ? e.getStatus().name() : "PENDING",
                    esc(e.getEnrollmentType()),
                    e.getEnrollmentDate() != null ? e.getEnrollmentDate().toString() : "NULL",
                    esc(e.getPreviousSchool()), schoolId));
        }
        sql.append("\n");

        sql.append("-- Avaliacoes\n");
        List<Assessment> assessments = assessmentRepository.findBySchoolId(schoolId);
        for (Assessment a : assessments) {
            sql.append(String.format("INSERT INTO assessments (id, name, type, school_class_id, subject_id, max_score, weight, date, trimester_id, school_id) VALUES (%d, '%s', '%s', %d, %d, %.1f, %.1f, '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    a.getId(), esc(a.getName()), esc(a.getType()),
                    a.getSchoolClass() != null ? a.getSchoolClass().getId() : 0,
                    a.getSubject() != null ? a.getSubject().getId() : 0,
                    a.getMaxScore(), a.getWeight(),
                    a.getDate() != null ? a.getDate().toString() : "NULL",
                    a.getTrimester() != null ? a.getTrimester().getId().toString() : "NULL",
                    schoolId));
        }
        sql.append("\n");

        sql.append("-- Notas\n");
        List<Grade> grades = gradeRepository.findBySchoolId(schoolId);
        for (Grade g : grades) {
            sql.append(String.format("INSERT INTO grades (id, student_id, assessment_id, score, observations, teacher_id, school_id) VALUES (%d, %d, %d, %.1f, '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    g.getId(),
                    g.getStudent() != null ? g.getStudent().getId() : 0,
                    g.getAssessment() != null ? g.getAssessment().getId() : 0,
                    g.getScore(), esc(g.getObservations()),
                    g.getGradedBy() != null ? g.getGradedBy().getId().toString() : "NULL",
                    schoolId));
        }
        sql.append("\n");

        sql.append("-- Assiduidade\n");
        List<Attendance> attendances = attendanceRepository.findBySchoolId(schoolId);
        for (Attendance a : attendances) {
            sql.append(String.format("INSERT INTO attendances (id, student_id, school_class_id, date, status, reason, teacher_id, school_id) VALUES (%d, %d, %d, '%s', '%s', '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    a.getId(),
                    a.getStudent() != null ? a.getStudent().getId() : 0,
                    a.getSchoolClass() != null ? a.getSchoolClass().getId() : 0,
                    a.getDate() != null ? a.getDate().toString() : "NULL",
                    a.getStatus() != null ? a.getStatus().name() : "NULL",
                    esc(a.getReason()),
                    a.getRecordedBy() != null ? a.getRecordedBy().getId().toString() : "NULL",
                    schoolId));
        }
        sql.append("\n");

        sql.append("-- Pagamentos\n");
        List<Payment> payments = paymentRepository.findBySchoolId(schoolId);
        for (Payment p : payments) {
            sql.append(String.format("INSERT INTO payments (id, receipt_number, student_id, payment_type, payment_method, amount, discount, fine, final_amount, status, payment_date, description, reference, month, academic_year_id, cash_received, change_amount, cash_register_name, cancelled, cancellation_reason, school_id) VALUES (%d, '%s', %d, '%s', '%s', %.2f, %.2f, %.2f, %.2f, '%s', '%s', '%s', '%s', '%s', %s, %.2f, %.2f, '%s', %s, '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    p.getId(), esc(p.getReceiptNumber()),
                    p.getStudent() != null ? p.getStudent().getId() : 0,
                    p.getPaymentType() != null ? p.getPaymentType().name() : "NULL",
                    p.getPaymentMethod() != null ? p.getPaymentMethod().name() : "NULL",
                    p.getAmount(), p.getDiscount(), p.getFine(), p.getFinalAmount(),
                    p.getStatus() != null ? p.getStatus().name() : "NULL",
                    p.getPaymentDate() != null ? p.getPaymentDate().toString() : "NULL",
                    esc(p.getDescription()), esc(p.getReference()), esc(p.getMonth()),
                    p.getAcademicYearId() != null ? p.getAcademicYearId().toString() : "NULL",
                    p.getCashReceived(), p.getChange(),
                    esc(p.getCashRegisterName()),
                    p.isCancelled(), esc(p.getCancellationReason()), schoolId));
        }
        sql.append("\n");

        sql.append("-- Anos Academicos\n");
        List<AcademicYear> years = academicYearRepository.findBySchoolId(schoolId);
        for (AcademicYear y : years) {
            sql.append(String.format("INSERT INTO academic_years (id, name, start_date, end_date, active, school_id) VALUES (%d, '%s', '%s', '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    y.getId(), esc(y.getName()),
                    y.getStartDate() != null ? y.getStartDate().toString() : "NULL",
                    y.getEndDate() != null ? y.getEndDate().toString() : "NULL",
                    y.isActive(), schoolId));
        }
        sql.append("\n");

        sql.append("-- Eventos do Calendario\n");
        List<CalendarEvent> events = calendarEventRepository.findBySchoolId(schoolId);
        for (CalendarEvent e : events) {
            sql.append(String.format("INSERT INTO calendar_events (id, name, start_date, end_date, type, academic_year_id, description, school_id) VALUES (%d, '%s', '%s', '%s', '%s', %s, '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    e.getId(), esc(e.getName()),
                    e.getStartDate() != null ? e.getStartDate().toString() : "NULL",
                    e.getEndDate() != null ? e.getEndDate().toString() : "NULL",
                    e.getType() != null ? e.getType().name() : "NULL",
                    e.getAcademicYear() != null ? e.getAcademicYear().getId().toString() : "NULL",
                    esc(e.getDescription()), schoolId));
        }
        sql.append("\n");

        sql.append("-- Precos de Servicos\n");
        List<ServicePrice> prices = servicePriceRepository.findBySchoolId(schoolId);
        for (ServicePrice p : prices) {
            sql.append(String.format("INSERT INTO service_prices (id, name, description, category, class_level, price, academic_year, due_day, fine_percent1, fine_day2, fine_percent2, fine_day3, fine_percent3, active, school_id) VALUES (%d, '%s', '%s', '%s', '%s', %.2f, '%s', %s, %s, %s, %s, %s, %s, %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    p.getId(), esc(p.getName()), esc(p.getDescription()),
                    p.getCategory() != null ? p.getCategory().name() : "NULL",
                    p.getClassLevel() != null ? p.getClassLevel().name() : "NULL",
                    p.getPrice(), esc(p.getAcademicYear()),
                    p.getDueDay() != null ? p.getDueDay().toString() : "NULL",
                    p.getFinePercent1() != null ? p.getFinePercent1().toString() : "NULL",
                    p.getFineDay2() != null ? p.getFineDay2().toString() : "NULL",
                    p.getFinePercent2() != null ? p.getFinePercent2().toString() : "NULL",
                    p.getFineDay3() != null ? p.getFineDay3().toString() : "NULL",
                    p.getFinePercent3() != null ? p.getFinePercent3().toString() : "NULL",
                    p.isActive(), schoolId));
        }
        sql.append("\n");

        sql.append("-- Documentos\n");
        List<Document> documents = documentRepository.findBySchoolId(schoolId);
        for (Document d : documents) {
            sql.append(String.format("INSERT INTO documents (id, title, subject_line, reference_line, module, status, category, document_type, format, file_path, department, description, uploaded_by, user_id, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    d.getId(), esc(d.getTitle()), esc(d.getSubject()), esc(d.getReference()),
                    d.getModule() != null ? d.getModule().name() : "NULL",
                    d.getStatus() != null ? d.getStatus().name() : "DRAFT",
                    esc(d.getCategory()), esc(d.getDocumentType()), esc(d.getFormat()),
                    esc(d.getFilePath()), esc(d.getDepartment()), esc(d.getDescription()),
                    esc(d.getUploadedBy()),
                    d.getUser() != null ? d.getUser().getId().toString() : "NULL",
                    schoolId));
        }
        sql.append("\n");

        sql.append("-- Biblioteca - Livros\n");
        List<LibraryBook> books = libraryBookRepository.findBySchoolId(schoolId);
        for (LibraryBook b : books) {
            sql.append(String.format("INSERT INTO library_books (id, code, isbn, title, author, category, publisher, year, total_copies, available_copies, location, status, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %d, %d, %d, '%s', '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    b.getId(), esc(b.getCode()), esc(b.getIsbn()), esc(b.getTitle()),
                    esc(b.getAuthor()), esc(b.getCategory()), esc(b.getPublisher()),
                    b.getYear(), b.getTotalCopies(), b.getAvailableCopies(),
                    esc(b.getLocation()),
                    b.getStatus() != null ? b.getStatus().name() : "AVAILABLE", schoolId));
        }
        sql.append("\n");

        sql.append("-- Biblioteca - Leitores\n");
        List<LibraryReader> readers = libraryReaderRepository.findBySchoolId(schoolId);
        for (LibraryReader r : readers) {
            sql.append(String.format("INSERT INTO library_readers (id, code, name, email, phone, type, status, class_name, document_id, address, birth_date, gender, profession, institution, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    r.getId(), esc(r.getCode()), esc(r.getName()), esc(r.getEmail()),
                    esc(r.getPhone()),
                    r.getType() != null ? r.getType().name() : "NULL",
                    r.getStatus() != null ? r.getStatus().name() : "ACTIVE",
                    esc(r.getClassName()), esc(r.getDocumentId()), esc(r.getAddress()),
                    r.getBirthDate() != null ? r.getBirthDate().toString() : "NULL",
                    r.getGender() != null ? r.getGender().name() : "NULL",
                    esc(r.getProfession()), esc(r.getInstitution()), schoolId));
        }
        sql.append("\n");

        sql.append("-- ========================================\n");
        sql.append("-- Backup concluido com sucesso\n");
        sql.append("-- Total: ").append(students.size()).append(" alunos, ")
                .append(teachers.size()).append(" professores, ")
                .append(classes.size()).append(" turmas, ")
                .append(payments.size()).append(" pagamentos\n");
        sql.append("-- ========================================\n");

        byte[] bytes = sql.toString().getBytes();
        String filename = "backup-" + school.getName().replaceAll(" ", "-") + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".sql";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBackupStats() {
        User user = getCurrentUser();
        if (user == null || user.getSchool() == null) {
            return ResponseEntity.badRequest().build();
        }
        Long schoolId = user.getSchool().getId();

        return ResponseEntity.ok(Map.of(
            "students", studentRepository.findBySchoolId(schoolId).size(),
            "teachers", teacherRepository.findBySchoolId(schoolId).size(),
            "classes", schoolClassRepository.findBySchoolId(schoolId).size(),
            "enrollments", enrollmentRepository.findBySchoolId(schoolId).size(),
            "grades", gradeRepository.findBySchoolId(schoolId).size(),
            "payments", paymentRepository.findBySchoolId(schoolId).size(),
            "subjects", subjectRepository.findBySchoolId(schoolId).size(),
            "documents", documentRepository.findBySchoolId(schoolId).size(),
            "users", userRepository.findBySchoolId(schoolId).size(),
            "lastBackup", LocalDateTime.now().toString()
        ));
    }

    private String esc(String s) {
        if (s == null) return "NULL";
        return "'" + s.replace("'", "''") + "'";
    }
}
