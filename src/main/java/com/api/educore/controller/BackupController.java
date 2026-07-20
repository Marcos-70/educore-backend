package com.api.educore.controller;

import com.api.educore.model.User;
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
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;

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

        StringBuilder sql = new StringBuilder();
        sql.append("-- Backup EduCore - Escola: ").append(user.getSchool().getName()).append("\n");
        sql.append("-- Data: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        // Export students
        var students = studentRepository.findBySchoolId(schoolId);
        for (var s : students) {
            sql.append(String.format("INSERT INTO students (id, first_name, last_name, date_of_birth, gender, email, phone, address, status, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', %d);\n",
                s.getId(), escape(s.getFirstName()), escape(s.getLastName()),
                s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : null,
                s.getGender() != null ? s.getGender().name() : null,
                escape(s.getEmail()), escape(s.getPhone()), escape(s.getAddress()),
                s.getStatus() != null ? s.getStatus().name() : "ACTIVE", schoolId));
        }

        // Export teachers
        var teachers = teacherRepository.findBySchoolId(schoolId);
        for (var t : teachers) {
            sql.append(String.format("INSERT INTO teachers (id, name, email, phone, gender, admission_date, status, school_id) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', %d);\n",
                t.getId(), escape(t.getName()), escape(t.getEmail()), escape(t.getPhone()),
                t.getGender() != null ? t.getGender().name() : null,
                t.getAdmissionDate() != null ? t.getAdmissionDate().toString() : null,
                t.getStatus() != null ? t.getStatus().name() : "ACTIVE", schoolId));
        }

        // Export school classes
        var classes = schoolClassRepository.findBySchoolId(schoolId);
        for (var c : classes) {
            sql.append(String.format("INSERT INTO school_classes (id, name, class_level, capacity, shift, academic_year, school_id) VALUES (%d, '%s', '%s', %d, '%s', '%s', %d);\n",
                c.getId(), escape(c.getName()), c.getClassLevel() != null ? c.getClassLevel().name() : null,
                c.getCapacity(), c.getShift() != null ? c.getShift().name() : null,
                escape(c.getAcademicYear()), schoolId));
        }

        // Export payments
        var payments = paymentRepository.findBySchoolId(schoolId);
        for (var p : payments) {
            sql.append(String.format("INSERT INTO payments (id, receipt_number, amount, payment_date, status, payment_type, payment_method, school_id) VALUES (%d, '%s', %.2f, '%s', '%s', '%s', '%s', %d);\n",
                p.getId(), escape(p.getReceiptNumber()), p.getAmount(),
                p.getPaymentDate() != null ? p.getPaymentDate().toString() : null,
                p.getStatus() != null ? p.getStatus().name() : null,
                p.getPaymentType() != null ? p.getPaymentType().name() : null,
                p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null, schoolId));
        }

        sql.append("\n-- Backup concluido com sucesso\n");

        byte[] bytes = sql.toString().getBytes();
        String filename = "backup-" + user.getSchool().getName().replaceAll(" ", "-") + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".sql";

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
            "payments", paymentRepository.findBySchoolId(schoolId).size(),
            "lastBackup", LocalDateTime.now().toString()
        ));
    }

    private String escape(String s) {
        if (s == null) return "NULL";
        return s.replace("'", "''");
    }
}
