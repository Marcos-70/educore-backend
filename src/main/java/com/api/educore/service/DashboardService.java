package com.api.educore.service;

import com.api.educore.dto.DashboardDTO;
import com.api.educore.model.*;
import com.api.educore.repository.EnrollmentRepository;
import com.api.educore.repository.PaymentRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ClassService classService;
    private final EnrollmentService enrollmentService;
    private final FinanceService financeService;
    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public DashboardDTO getDashboard() {
        School school = getCurrentSchool();
        List<DashboardDTO.RecentActivity> recentActivities = school != null
                ? getRecentActivities(school.getId())
                : List.of();

        return DashboardDTO.builder()
                .totalStudents(studentService.count())
                .totalTeachers(teacherService.count())
                .totalClasses(classService.count())
                .activeEnrollments(enrollmentService.countByStatus(EnrollmentStatus.APPROVED))
                .totalRevenue(financeService.getTotalCollected())
                .totalPending(financeService.getTotalPending())
                .paidPayments(financeService.countPaid())
                .unpaidPayments(financeService.countUnpaid())
                .recentActivities(recentActivities)
                .topDebtors(getTopDebtors())
                .build();
    }

    private record ActivityEntry(String type, String description, LocalDateTime timestamp) {}

    private List<DashboardDTO.RecentActivity> getRecentActivities(Long schoolId) {
        List<ActivityEntry> entries = new ArrayList<>();

        List<Payment> recentPayments = paymentRepository.findRecentBySchoolId(schoolId);
        for (Payment p : recentPayments) {
            String studentName = p.getStudent() != null ? p.getStudent().getFullName() : "Desconhecido";
            String description;
            String type;

            if (p.isCancelled()) {
                type = "cancel";
                description = "Factura " + p.getReceiptNumber() + " anulada — " + studentName;
            } else if (p.getStatus() == PaymentStatus.PAID) {
                type = "payment";
                description = "Pagamento de " + studentName + " — " + formatCurrency(p.getFinalAmount());
            } else {
                type = "invoice";
                description = "Factura " + p.getReceiptNumber() + " emitida para " + studentName;
            }

            LocalDateTime timestamp = p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now();
            entries.add(new ActivityEntry(type, description, timestamp));
        }

        List<Enrollment> recentEnrollments = enrollmentRepository.findBySchoolId(schoolId).stream()
                .filter(e -> e.getCreatedAt() != null)
                .sorted(Comparator.comparing(Enrollment::getCreatedAt).reversed())
                .limit(5)
                .toList();
        for (Enrollment e : recentEnrollments) {
            String studentName = e.getStudent() != null ? e.getStudent().getFullName() : "Desconhecido";
            String className = e.getSchoolClass() != null ? e.getSchoolClass().getName() : "";
            String statusLabel = switch (e.getStatus()) {
                case APPROVED -> "aprovada";
                case CONFIRMED -> "confirmada";
                case PENDING -> "pendente";
                case REJECTED -> "rejeitada";
                case CANCELLED -> "cancelada";
            };
            String desc = "Matrícula " + statusLabel + " — " + studentName + (!className.isEmpty() ? " (" + className + ")" : "");
            entries.add(new ActivityEntry("attendance", desc, e.getCreatedAt()));
        }

        entries.sort(Comparator.comparing(ActivityEntry::timestamp).reversed());
        return entries.stream()
                .limit(10)
                .map(e -> new DashboardDTO.RecentActivity(e.type(), e.description(), formatTimeAgo(e.timestamp())))
                .collect(Collectors.toList());
    }

    private String formatCurrency(double value) {
        return String.format("%,.0f Kz", value);
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Agora";
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) return "Agora";
        if (minutes < 60) return "Há " + minutes + " min";
        long hours = duration.toHours();
        if (hours < 24) return "Há " + hours + " h";
        long days = duration.toDays();
        if (days < 7) return "Há " + days + " dia" + (days > 1 ? "s" : "");
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private List<DashboardDTO.TopDebtor> getTopDebtors() {
        School school = getCurrentSchool();
        if (school == null) return List.of();

        List<Object[]> results = paymentRepository.findTopDebtorsBySchoolId(school.getId());
        return results.stream()
                .limit(10)
                .map(row -> new DashboardDTO.TopDebtor(
                        row[1] + " " + row[2],
                        row[3] != null ? row[3].toString() : "N/A",
                        ((Number) row[4]).intValue(),
                        ((Number) row[5]).doubleValue()
                ))
                .collect(Collectors.toList());
    }
}
