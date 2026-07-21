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
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final TrimesterRepository trimesterRepository;
    private final UserPermissionRepository userPermissionRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @GetMapping("/sql")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
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

        // ============ SCHOOLS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: schools\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS schools CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS schools (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  nif VARCHAR(255),\n");
        sql.append("  address VARCHAR(255),\n");
        sql.append("  city VARCHAR(255),\n");
        sql.append("  country VARCHAR(255),\n");
        sql.append("  email VARCHAR(255),\n");
        sql.append("  phone VARCHAR(255),\n");
        sql.append("  website VARCHAR(255),\n");
        sql.append("  logo TEXT,\n");
        sql.append("  director_id BIGINT,\n");
        sql.append("  motto VARCHAR(255),\n");
        sql.append("  active BOOLEAN DEFAULT TRUE\n");
        sql.append(");\n\n");

        sql.append(String.format("INSERT INTO schools (id, name, nif, address, city, country, email, phone, website, logo, motto, active, director_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', NULL, '%s', %s, %s) ON CONFLICT (id) DO NOTHING;\n\n",
                school.getId(), esc(school.getName()), esc(school.getNif()), esc(school.getAddress()),
                esc(school.getCity()), esc(school.getCountry()), esc(school.getEmail()),
                esc(school.getPhone()), esc(school.getWebsite()), esc(school.getMotto()),
                school.isActive(), school.getDirector() != null ? school.getDirector().getId() : "NULL"));

        // ============ USERS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: users\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS users CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS users (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  email VARCHAR(255) NOT NULL UNIQUE,\n");
        sql.append("  password VARCHAR(255) NOT NULL,\n");
        sql.append("  role VARCHAR(50) NOT NULL,\n");
        sql.append("  phone VARCHAR(255),\n");
        sql.append("  avatar TEXT,\n");
        sql.append("  address VARCHAR(255),\n");
        sql.append("  city VARCHAR(255),\n");
        sql.append("  country VARCHAR(255),\n");
        sql.append("  bi_number VARCHAR(255),\n");
        sql.append("  date_of_birth VARCHAR(255),\n");
        sql.append("  gender VARCHAR(50),\n");
        sql.append("  active BOOLEAN DEFAULT TRUE,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

        List<User> users = userRepository.findBySchoolId(schoolId);
        for (User u : users) {
            sql.append(String.format("INSERT INTO users (id, first_name, last_name, username, email, password, role, position, phone, avatar, address, gender, active, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', NULL, '%s', '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    u.getId(), esc(u.getFirstName()), esc(u.getLastName()), esc(u.getUsername()),
                    esc(u.getEmail()), esc(u.getPassword()),
                    u.getRole() != null ? u.getRole().name() : "SECRETARIO",
                    esc(u.getPosition()), esc(u.getPhone()), esc(u.getAddress()), esc(u.getGender()),
                    u.isActive(), schoolId));
        }
        sql.append("\n");

        // ============ SCHOOL_SETTINGS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: school_settings\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS school_settings CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS school_settings (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  school_name VARCHAR(255),\n");
        sql.append("  school_motto VARCHAR(255),\n");
        sql.append("  nif VARCHAR(255),\n");
        sql.append("  address VARCHAR(255),\n");
        sql.append("  email VARCHAR(255),\n");
        sql.append("  phone VARCHAR(255),\n");
        sql.append("  currency VARCHAR(50),\n");
        sql.append("  timezone VARCHAR(100),\n");
        sql.append("  logo_path TEXT,\n");
        sql.append("  active_academic_year VARCHAR(100),\n");
        sql.append("  auto_billing BOOLEAN DEFAULT TRUE,\n");
        sql.append("  auto_backup BOOLEAN DEFAULT TRUE,\n");
        sql.append("  email_notifications BOOLEAN DEFAULT TRUE,\n");
        sql.append("  sms_notifications BOOLEAN DEFAULT FALSE,\n");
        sql.append("  push_notifications BOOLEAN DEFAULT TRUE,\n");
        sql.append("  language VARCHAR(10) DEFAULT 'pt',\n");
        sql.append("  date_format VARCHAR(20),\n");
        sql.append("  currency_format VARCHAR(50),\n");
        sql.append("  theme VARCHAR(20) DEFAULT 'light',\n");
        sql.append("  compact_mode BOOLEAN DEFAULT FALSE,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ STUDENTS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: students\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS students CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS students (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  first_name VARCHAR(255) NOT NULL,\n");
        sql.append("  last_name VARCHAR(255),\n");
        sql.append("  student_number VARCHAR(100),\n");
        sql.append("  date_of_birth DATE,\n");
        sql.append("  gender VARCHAR(50),\n");
        sql.append("  nationality VARCHAR(100),\n");
        sql.append("  nif VARCHAR(100),\n");
        sql.append("  email VARCHAR(255),\n");
        sql.append("  phone VARCHAR(100),\n");
        sql.append("  address VARCHAR(255),\n");
        sql.append("  photo TEXT,\n");
        sql.append("  guardian_name VARCHAR(255),\n");
        sql.append("  guardian_contact VARCHAR(100),\n");
        sql.append("  guardian_email VARCHAR(255),\n");
        sql.append("  relationship VARCHAR(100),\n");
        sql.append("  previous_school VARCHAR(255),\n");
        sql.append("  emergency_contact VARCHAR(100),\n");
        sql.append("  emergency_phone VARCHAR(100),\n");
        sql.append("  allergies TEXT,\n");
        sql.append("  medical_notes TEXT,\n");
        sql.append("  status VARCHAR(50) DEFAULT 'ACTIVE',\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ TEACHERS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: teachers\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS teachers CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS teachers (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  email VARCHAR(255),\n");
        sql.append("  phone VARCHAR(100),\n");
        sql.append("  gender VARCHAR(50),\n");
        sql.append("  birth_date DATE,\n");
        sql.append("  bi VARCHAR(100),\n");
        sql.append("  admission_date DATE,\n");
        sql.append("  status VARCHAR(50) DEFAULT 'ACTIVE',\n");
        sql.append("  qualification VARCHAR(255),\n");
        sql.append("  formation_area VARCHAR(255),\n");
        sql.append("  department VARCHAR(255),\n");
        sql.append("  subject VARCHAR(255),\n");
        sql.append("  address VARCHAR(255),\n");
        sql.append("  bio TEXT,\n");
        sql.append("  photo TEXT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ SCHOOL_CLASSES ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: school_classes\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS school_classes CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS school_classes (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  class_level VARCHAR(50),\n");
        sql.append("  course VARCHAR(255),\n");
        sql.append("  room VARCHAR(100),\n");
        sql.append("  capacity INT DEFAULT 0,\n");
        sql.append("  shift VARCHAR(50),\n");
        sql.append("  academic_year VARCHAR(50),\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ SUBJECTS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: subjects\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS subjects CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS subjects (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  code VARCHAR(50),\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  abbrev_name VARCHAR(50),\n");
        sql.append("  description TEXT,\n");
        sql.append("  class_level VARCHAR(50),\n");
        sql.append("  type VARCHAR(50),\n");
        sql.append("  minimum_grade DECIMAL(5,1) DEFAULT 10.0,\n");
        sql.append("  minimum_attendance INT DEFAULT 75,\n");
        sql.append("  active BOOLEAN DEFAULT TRUE,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ ACADEMIC_YEARS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: academic_years\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS academic_years CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS academic_years (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(100) NOT NULL,\n");
        sql.append("  start_date DATE,\n");
        sql.append("  end_date DATE,\n");
        sql.append("  active BOOLEAN DEFAULT FALSE,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

        List<AcademicYear> years = academicYearRepository.findBySchoolId(schoolId);
        for (AcademicYear y : years) {
            sql.append(String.format("INSERT INTO academic_years (id, name, start_date, end_date, active, school_id) VALUES (%d, '%s', '%s', '%s', %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    y.getId(), esc(y.getName()),
                    y.getStartDate() != null ? y.getStartDate().toString() : "NULL",
                    y.getEndDate() != null ? y.getEndDate().toString() : "NULL",
                    y.isActive(), schoolId));
        }
        sql.append("\n");

        // ============ TRIMESTERS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: trimesters\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS trimesters CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS trimesters (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(100) NOT NULL,\n");
        sql.append("  start_date DATE,\n");
        sql.append("  end_date DATE,\n");
        sql.append("  academic_year_id BIGINT\n");
        sql.append(");\n\n");

        List<Trimester> trimesters = trimesterRepository.findAll().stream().filter(t -> t.getSchool() != null && t.getSchool().getId().equals(schoolId)).toList();
        for (Trimester t : trimesters) {
            sql.append(String.format("INSERT INTO trimesters (id, name, start_date, end_date, academic_year_id) VALUES (%d, '%s', '%s', '%s', %s) ON CONFLICT (id) DO NOTHING;\n",
                    t.getId(), esc(t.getName()),
                    t.getStartDate() != null ? t.getStartDate().toString() : "NULL",
                    t.getEndDate() != null ? t.getEndDate().toString() : "NULL",
                    t.getAcademicYear() != null ? t.getAcademicYear().getId().toString() : "NULL"));
        }
        sql.append("\n");

        // ============ ENROLLMENTS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: enrollments\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS enrollments CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS enrollments (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  student_id BIGINT,\n");
        sql.append("  class_id BIGINT,\n");
        sql.append("  academic_year VARCHAR(50),\n");
        sql.append("  status VARCHAR(50) DEFAULT 'PENDING',\n");
        sql.append("  enrollment_type VARCHAR(50),\n");
        sql.append("  enrollment_date DATE,\n");
        sql.append("  photo TEXT,\n");
        sql.append("  previous_school VARCHAR(255),\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ ASSESSMENTS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: assessments\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS assessments CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS assessments (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  type VARCHAR(100),\n");
        sql.append("  school_class_id BIGINT,\n");
        sql.append("  subject_id BIGINT,\n");
        sql.append("  max_score DECIMAL(5,1) DEFAULT 20.0,\n");
        sql.append("  weight DECIMAL(5,2) DEFAULT 1.0,\n");
        sql.append("  date DATE,\n");
        sql.append("  trimester_id BIGINT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ GRADES ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: grades\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS grades CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS grades (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  student_id BIGINT,\n");
        sql.append("  assessment_id BIGINT,\n");
        sql.append("  score DECIMAL(5,2),\n");
        sql.append("  observations TEXT,\n");
        sql.append("  teacher_id BIGINT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ ATTENDANCES ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: attendances\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS attendances CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS attendances (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  student_id BIGINT,\n");
        sql.append("  school_class_id BIGINT,\n");
        sql.append("  date DATE,\n");
        sql.append("  status VARCHAR(50),\n");
        sql.append("  reason TEXT,\n");
        sql.append("  teacher_id BIGINT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ PAYMENTS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: payments\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS payments CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS payments (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  receipt_number VARCHAR(100),\n");
        sql.append("  student_id BIGINT,\n");
        sql.append("  payment_type VARCHAR(50),\n");
        sql.append("  payment_method VARCHAR(50),\n");
        sql.append("  amount DECIMAL(12,2) DEFAULT 0,\n");
        sql.append("  discount DECIMAL(12,2) DEFAULT 0,\n");
        sql.append("  fine DECIMAL(12,2) DEFAULT 0,\n");
        sql.append("  final_amount DECIMAL(12,2) DEFAULT 0,\n");
        sql.append("  status VARCHAR(50),\n");
        sql.append("  payment_date DATE,\n");
        sql.append("  description TEXT,\n");
        sql.append("  reference VARCHAR(255),\n");
        sql.append("  month VARCHAR(50),\n");
        sql.append("  academic_year_id BIGINT,\n");
        sql.append("  cash_received DECIMAL(12,2) DEFAULT 0,\n");
        sql.append("  change_amount DECIMAL(12,2) DEFAULT 0,\n");
        sql.append("  cash_register_name VARCHAR(255),\n");
        sql.append("  cancelled BOOLEAN DEFAULT FALSE,\n");
        sql.append("  cancellation_reason TEXT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ CALENDAR_EVENTS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: calendar_events\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS calendar_events CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS calendar_events (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  start_date DATE,\n");
        sql.append("  end_date DATE,\n");
        sql.append("  type VARCHAR(50),\n");
        sql.append("  academic_year_id BIGINT,\n");
        sql.append("  description TEXT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ CLASS_SCHEDULES ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: class_schedules\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS class_schedules CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS class_schedules (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  day_of_week VARCHAR(20),\n");
        sql.append("  start_time TIME,\n");
        sql.append("  end_time TIME,\n");
        sql.append("  class_id BIGINT,\n");
        sql.append("  subject_id BIGINT,\n");
        sql.append("  teacher_id BIGINT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

        List<ClassSchedule> schedules = classScheduleRepository.findBySchoolId(schoolId);
        for (ClassSchedule cs : schedules) {
            sql.append(String.format("INSERT INTO class_schedules (id, day_of_week, start_time, end_time, class_id, subject_id, teacher_id, school_id) VALUES (%d, '%s', '%s', '%s', %d, %d, %s, %d) ON CONFLICT (id) DO NOTHING;\n",
                    cs.getId(),
                    esc(cs.getDayOfWeek()),
                    cs.getStartTime() != null ? cs.getStartTime().toString() : "NULL",
                    cs.getEndTime() != null ? cs.getEndTime().toString() : "NULL",
                    cs.getSchoolClass() != null ? cs.getSchoolClass().getId() : 0,
                    cs.getSubject() != null ? cs.getSubject().getId() : 0,
                    cs.getTeacher() != null ? cs.getTeacher().getId().toString() : "NULL",
                    schoolId));
        }
        sql.append("\n");

        // ============ SERVICE_PRICES ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: service_prices\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS service_prices CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS service_prices (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  description TEXT,\n");
        sql.append("  category VARCHAR(100),\n");
        sql.append("  class_level VARCHAR(50),\n");
        sql.append("  price DECIMAL(12,2) DEFAULT 0,\n");
        sql.append("  academic_year VARCHAR(50),\n");
        sql.append("  due_day INT,\n");
        sql.append("  fine_percent1 DECIMAL(5,2),\n");
        sql.append("  fine_day2 INT,\n");
        sql.append("  fine_percent2 DECIMAL(5,2),\n");
        sql.append("  fine_day3 INT,\n");
        sql.append("  fine_percent3 DECIMAL(5,2),\n");
        sql.append("  active BOOLEAN DEFAULT TRUE,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ DOCUMENTS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: documents\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS documents CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS documents (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  title VARCHAR(255) NOT NULL,\n");
        sql.append("  subject_line VARCHAR(255),\n");
        sql.append("  reference_line VARCHAR(255),\n");
        sql.append("  module VARCHAR(100),\n");
        sql.append("  status VARCHAR(50) DEFAULT 'DRAFT',\n");
        sql.append("  category VARCHAR(100),\n");
        sql.append("  document_type VARCHAR(100),\n");
        sql.append("  format VARCHAR(50),\n");
        sql.append("  file_path TEXT,\n");
        sql.append("  department VARCHAR(255),\n");
        sql.append("  description TEXT,\n");
        sql.append("  uploaded_by VARCHAR(255),\n");
        sql.append("  user_id BIGINT,\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ LIBRARY_BOOKS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: library_books\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS library_books CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS library_books (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  code VARCHAR(100),\n");
        sql.append("  isbn VARCHAR(100),\n");
        sql.append("  title VARCHAR(255) NOT NULL,\n");
        sql.append("  author VARCHAR(255),\n");
        sql.append("  category VARCHAR(100),\n");
        sql.append("  publisher VARCHAR(255),\n");
        sql.append("  year INT,\n");
        sql.append("  total_copies INT DEFAULT 1,\n");
        sql.append("  available_copies INT DEFAULT 1,\n");
        sql.append("  location VARCHAR(255),\n");
        sql.append("  status VARCHAR(50) DEFAULT 'AVAILABLE',\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ LIBRARY_READERS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: library_readers\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS library_readers CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS library_readers (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  code VARCHAR(100),\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  email VARCHAR(255),\n");
        sql.append("  phone VARCHAR(100),\n");
        sql.append("  type VARCHAR(50),\n");
        sql.append("  status VARCHAR(50) DEFAULT 'ACTIVE',\n");
        sql.append("  class_name VARCHAR(100),\n");
        sql.append("  document_id VARCHAR(100),\n");
        sql.append("  address VARCHAR(255),\n");
        sql.append("  birth_date DATE,\n");
        sql.append("  gender VARCHAR(50),\n");
        sql.append("  profession VARCHAR(255),\n");
        sql.append("  institution VARCHAR(255),\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

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

        // ============ LIBRARY_LOANS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: library_loans\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS library_loans CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS library_loans (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  book_id BIGINT,\n");
        sql.append("  reader_id BIGINT,\n");
        sql.append("  loan_date DATE,\n");
        sql.append("  due_date DATE,\n");
        sql.append("  return_date DATE,\n");
        sql.append("  status VARCHAR(50) DEFAULT 'ACTIVE',\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

        List<LibraryLoan> loans = libraryLoanRepository.findBySchoolId(schoolId);
        for (LibraryLoan l : loans) {
            sql.append(String.format("INSERT INTO library_loans (id, book_id, reader_id, loan_date, due_date, return_date, status, school_id) VALUES (%d, %s, %s, '%s', '%s', %s, '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    l.getId(),
                    l.getBook() != null ? l.getBook().getId().toString() : "NULL",
                    l.getReader() != null ? l.getReader().getId().toString() : "NULL",
                    l.getLoanDate() != null ? l.getLoanDate().toString() : "NULL",
                    l.getDueDate() != null ? l.getDueDate().toString() : "NULL",
                    l.getReturnDate() != null ? l.getReturnDate().toString() : "NULL",
                    l.getStatus() != null ? l.getStatus().name() : "ACTIVE",
                    schoolId));
        }
        sql.append("\n");

        // ============ BUSES ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: buses\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS buses CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS buses (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  plate_number VARCHAR(50),\n");
        sql.append("  capacity INT DEFAULT 0,\n");
        sql.append("  model VARCHAR(100),\n");
        sql.append("  brand VARCHAR(100),\n");
        sql.append("  status VARCHAR(50) DEFAULT 'ACTIVE',\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

        List<Bus> buses = busRepository.findBySchoolId(schoolId);
        for (Bus b : buses) {
            sql.append(String.format("INSERT INTO buses (id, plate_number, capacity, model, brand, status, school_id) VALUES (%d, '%s', %d, '%s', '%s', '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    b.getId(), esc(b.getPlateNumber()), b.getCapacity(),
                    esc(b.getModel()), esc(b.getBrand()),
                    b.getStatus() != null ? b.getStatus().name() : "ACTIVE",
                    schoolId));
        }
        sql.append("\n");

        // ============ DRIVERS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: drivers\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS drivers CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS drivers (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  phone VARCHAR(100),\n");
        sql.append("  license_number VARCHAR(100),\n");
        sql.append("  bus_id BIGINT,\n");
        sql.append("  status VARCHAR(50) DEFAULT 'ACTIVE',\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

        List<Driver> drivers = driverRepository.findBySchoolId(schoolId);
        for (Driver d : drivers) {
            sql.append(String.format("INSERT INTO drivers (id, name, phone, license_number, bus_id, status, school_id) VALUES (%d, '%s', '%s', '%s', %s, '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    d.getId(), esc(d.getName()), esc(d.getPhone()), esc(d.getLicenseNumber()),
                    d.getAssignedBus() != null ? d.getAssignedBus().getId().toString() : "NULL",
                    d.getStatus() != null ? d.getStatus().name() : "ACTIVE",
                    schoolId));
        }
        sql.append("\n");

        // ============ TRANSPORT_ROUTES ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: transport_routes\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS transport_routes CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS transport_routes (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  name VARCHAR(255) NOT NULL,\n");
        sql.append("  description TEXT,\n");
        sql.append("  bus_id BIGINT,\n");
        sql.append("  driver_id BIGINT,\n");
        sql.append("  route_text TEXT,\n");
        sql.append("  status VARCHAR(50) DEFAULT 'ACTIVE',\n");
        sql.append("  school_id BIGINT\n");
        sql.append(");\n\n");

        List<TransportRoute> routes = transportRouteRepository.findBySchoolId(schoolId);
        for (TransportRoute r : routes) {
            sql.append(String.format("INSERT INTO transport_routes (id, name, description, bus_id, driver_id, route_text, status, school_id) VALUES (%d, '%s', '%s', %s, %s, '%s', '%s', %d) ON CONFLICT (id) DO NOTHING;\n",
                    r.getId(), esc(r.getName()), esc(r.getDescription()),
                    r.getBus() != null ? r.getBus().getId().toString() : "NULL",
                    r.getDriver() != null ? r.getDriver().getId().toString() : "NULL",
                    esc(r.getDescription()),
                    r.getStatus() != null ? r.getStatus().name() : "ACTIVE",
                    schoolId));
        }
        sql.append("\n");

        // ============ USER_PERMISSIONS ============
        sql.append("-- ========================================\n");
        sql.append("-- Tabela: user_permissions\n");
        sql.append("-- ========================================\n");
        sql.append("DROP TABLE IF EXISTS user_permissions CASCADE;\n");
        sql.append("CREATE TABLE IF NOT EXISTS user_permissions (\n");
        sql.append("  id BIGSERIAL PRIMARY KEY,\n");
        sql.append("  user_id BIGINT NOT NULL,\n");
        sql.append("  module VARCHAR(100) NOT NULL,\n");
        sql.append("  enabled BOOLEAN DEFAULT TRUE\n");
        sql.append(");\n\n");

        List<UserPermission> permissions = new java.util.ArrayList<>();
        for (User u : users) {
            permissions.addAll(userPermissionRepository.findByUserId(u.getId()));
        }
        for (UserPermission up : permissions) {
            sql.append(String.format("INSERT INTO user_permissions (id, user_id, permission, enabled) VALUES (%d, %d, '%s', %s) ON CONFLICT (id) DO NOTHING;\n",
                    up.getId(),
                    up.getUser() != null ? up.getUser().getId() : 0,
                    esc(up.getPermission().name()),
                    up.isEnabled()));
        }
        sql.append("\n");

        // ============ SUMMARY ============
        sql.append("-- ========================================\n");
        sql.append("-- Resumo do Backup\n");
        sql.append("-- ========================================\n");
        sql.append("-- Total de registos por tabela:\n");
        sql.append("-- schools: 1\n");
        sql.append("-- users: ").append(users.size()).append("\n");
        sql.append("-- school_settings: ").append(settings != null ? 1 : 0).append("\n");
        sql.append("-- students: ").append(students.size()).append("\n");
        sql.append("-- teachers: ").append(teachers.size()).append("\n");
        sql.append("-- school_classes: ").append(classes.size()).append("\n");
        sql.append("-- subjects: ").append(subjects.size()).append("\n");
        sql.append("-- academic_years: ").append(years.size()).append("\n");
        sql.append("-- trimesters: ").append(trimesters.size()).append("\n");
        sql.append("-- enrollments: ").append(enrollments.size()).append("\n");
        sql.append("-- assessments: ").append(assessments.size()).append("\n");
        sql.append("-- grades: ").append(grades.size()).append("\n");
        sql.append("-- attendances: ").append(attendances.size()).append("\n");
        sql.append("-- payments: ").append(payments.size()).append("\n");
        sql.append("-- calendar_events: ").append(events.size()).append("\n");
        sql.append("-- class_schedules: ").append(schedules.size()).append("\n");
        sql.append("-- service_prices: ").append(prices.size()).append("\n");
        sql.append("-- documents: ").append(documents.size()).append("\n");
        sql.append("-- library_books: ").append(books.size()).append("\n");
        sql.append("-- library_readers: ").append(readers.size()).append("\n");
        sql.append("-- library_loans: ").append(loans.size()).append("\n");
        sql.append("-- buses: ").append(buses.size()).append("\n");
        sql.append("-- drivers: ").append(drivers.size()).append("\n");
        sql.append("-- transport_routes: ").append(routes.size()).append("\n");
        sql.append("-- user_permissions: ").append(permissions.size()).append("\n");
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
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
