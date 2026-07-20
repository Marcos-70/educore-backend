package com.api.educore.controller;

import com.api.educore.dto.ResetRequest;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;
    private final SubjectRepository subjectRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final DocumentRepository documentRepository;
    private final LibraryBookRepository libraryBookRepository;
    private final LibraryLoanRepository libraryLoanRepository;
    private final LibraryReaderRepository libraryReaderRepository;
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AssessmentRepository assessmentRepository;
    private final ServicePriceRepository servicePriceRepository;
    private final UserPermissionRepository userPermissionRepository;

    @PostMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetData(@RequestBody ResetRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null || currentUser.getSchool() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario ou escola nao encontrada"));
        }

        Long schoolId = currentUser.getSchool().getId();
        List<String> modules = request.getModules();
        if (modules == null || modules.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhum modulo especificado"));
        }

        int totalDeleted = 0;

        // Delete in dependency order: leaf entities first, then parents
        if (modules.contains("attendance")) totalDeleted += deleteAttendance(schoolId);
        if (modules.contains("grades")) totalDeleted += deleteGrades(schoolId);
        if (modules.contains("payments")) totalDeleted += deletePayments(schoolId);
        if (modules.contains("enrollments")) totalDeleted += deleteEnrollments(schoolId);
        if (modules.contains("schedules")) totalDeleted += deleteSchedules(schoolId);
        if (modules.contains("library")) totalDeleted += deleteLibrary(schoolId);
        if (modules.contains("transport")) totalDeleted += deleteTransport(schoolId);
        if (modules.contains("documents")) totalDeleted += deleteDocuments(schoolId);
        if (modules.contains("calendar")) totalDeleted += deleteCalendar(schoolId);
        if (modules.contains("assessments")) totalDeleted += deleteAssessments(schoolId);
        if (modules.contains("subjects")) totalDeleted += deleteSubjects(schoolId);
        if (modules.contains("classes")) totalDeleted += deleteClasses(schoolId);
        if (modules.contains("students")) totalDeleted += deleteStudents(schoolId);
        if (modules.contains("teachers")) totalDeleted += deleteTeachers(schoolId);
        if (modules.contains("academic")) totalDeleted += deleteAcademic(schoolId);
        if (modules.contains("prices")) totalDeleted += deletePrices(schoolId);
        if (modules.contains("permissions")) totalDeleted += deletePermissions(schoolId);
        if (modules.contains("users")) totalDeleted += deleteUsers(schoolId, currentUser.getId());

        return ResponseEntity.ok(Map.of(
            "message", totalDeleted + " registros apagados com sucesso",
            "deleted", String.valueOf(totalDeleted)
        ));
    }

    private int deleteStudents(Long schoolId) {
        List<Student> items = studentRepository.findBySchoolId(schoolId);
        studentRepository.deleteAll(items);
        return items.size();
    }
    private int deleteTeachers(Long schoolId) {
        List<Teacher> items = teacherRepository.findBySchoolId(schoolId);
        teacherRepository.deleteAll(items);
        return items.size();
    }
    private int deleteClasses(Long schoolId) {
        List<SchoolClass> items = schoolClassRepository.findBySchoolId(schoolId);
        schoolClassRepository.deleteAll(items);
        return items.size();
    }
    private int deleteEnrollments(Long schoolId) {
        List<Enrollment> items = enrollmentRepository.findBySchoolId(schoolId);
        enrollmentRepository.deleteAll(items);
        return items.size();
    }
    private int deleteGrades(Long schoolId) {
        List<Grade> items = gradeRepository.findBySchoolId(schoolId);
        gradeRepository.deleteAll(items);
        return items.size();
    }
    private int deleteAttendance(Long schoolId) {
        List<Attendance> items = attendanceRepository.findBySchoolId(schoolId);
        attendanceRepository.deleteAll(items);
        return items.size();
    }
    private int deletePayments(Long schoolId) {
        List<Payment> items = paymentRepository.findBySchoolId(schoolId);
        paymentRepository.deleteAll(items);
        return items.size();
    }
    private int deleteSubjects(Long schoolId) {
        List<Subject> items = subjectRepository.findBySchoolId(schoolId);
        subjectRepository.deleteAll(items);
        return items.size();
    }
    private int deleteSchedules(Long schoolId) {
        List<ClassSchedule> items = classScheduleRepository.findBySchoolId(schoolId);
        classScheduleRepository.deleteAll(items);
        return items.size();
    }
    private int deleteDocuments(Long schoolId) {
        List<Document> items = documentRepository.findBySchoolId(schoolId);
        documentRepository.deleteAll(items);
        return items.size();
    }
    private int deleteLibrary(Long schoolId) {
        int count = 0;
        List<LibraryLoan> loans = libraryLoanRepository.findBySchoolId(schoolId);
        libraryLoanRepository.deleteAll(loans);
        count += loans.size();
        List<LibraryReader> readers = libraryReaderRepository.findBySchoolId(schoolId);
        libraryReaderRepository.deleteAll(readers);
        count += readers.size();
        List<LibraryBook> books = libraryBookRepository.findBySchoolId(schoolId);
        libraryBookRepository.deleteAll(books);
        count += books.size();
        return count;
    }
    private int deleteTransport(Long schoolId) {
        int count = 0;
        List<TransportRoute> routes = transportRouteRepository.findBySchoolId(schoolId);
        transportRouteRepository.deleteAll(routes);
        count += routes.size();
        List<Driver> drivers = driverRepository.findBySchoolId(schoolId);
        driverRepository.deleteAll(drivers);
        count += drivers.size();
        List<Bus> buses = busRepository.findBySchoolId(schoolId);
        busRepository.deleteAll(buses);
        count += buses.size();
        return count;
    }
    private int deleteCalendar(Long schoolId) {
        List<CalendarEvent> items = calendarEventRepository.findBySchoolId(schoolId);
        calendarEventRepository.deleteAll(items);
        return items.size();
    }
    private int deleteAcademic(Long schoolId) {
        int count = 0;
        List<AcademicYear> years = academicYearRepository.findBySchoolId(schoolId);
        academicYearRepository.deleteAll(years);
        count += years.size();
        return count;
    }
    private int deleteAssessments(Long schoolId) {
        List<Assessment> items = assessmentRepository.findBySchoolId(schoolId);
        assessmentRepository.deleteAll(items);
        return items.size();
    }
    private int deletePrices(Long schoolId) {
        List<ServicePrice> items = servicePriceRepository.findBySchoolId(schoolId);
        servicePriceRepository.deleteAll(items);
        return items.size();
    }
    private int deleteUsers(Long schoolId, Long currentUserId) {
        List<User> users = userRepository.findBySchoolId(schoolId);
        int count = 0;
        for (User u : users) {
            if (!u.getId().equals(currentUserId)) {
                userRepository.delete(u);
                count++;
            }
        }
        return count;
    }
    private int deletePermissions(Long schoolId) {
        List<User> users = userRepository.findBySchoolId(schoolId);
        int count = 0;
        for (User u : users) {
            List<UserPermission> perms = userPermissionRepository.findByUserId(u.getId());
            userPermissionRepository.deleteAll(perms);
            count += perms.size();
        }
        return count;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        if (currentUser == null || currentUser.getSchool() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Usuario nao encontrado"));
        }
        Long schoolId = currentUser.getSchool().getId();

        return ResponseEntity.ok(Map.of(
            "students", studentRepository.findBySchoolId(schoolId).size(),
            "teachers", teacherRepository.findBySchoolId(schoolId).size(),
            "classes", schoolClassRepository.findBySchoolId(schoolId).size(),
            "enrollments", enrollmentRepository.findBySchoolId(schoolId).size(),
            "payments", paymentRepository.findBySchoolId(schoolId).size(),
            "users", userRepository.findBySchoolId(schoolId).size(),
            "libraryBooks", libraryBookRepository.findBySchoolId(schoolId).size(),
            "documents", documentRepository.findBySchoolId(schoolId).size()
        ));
    }
}
