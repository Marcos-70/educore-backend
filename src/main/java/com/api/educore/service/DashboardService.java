package com.api.educore.service;

import com.api.educore.dto.DashboardDTO;
import com.api.educore.model.EnrollmentStatus;
import com.api.educore.model.School;
import com.api.educore.model.User;
import com.api.educore.repository.PaymentRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public DashboardDTO getDashboard() {
        return DashboardDTO.builder()
                .totalStudents(studentService.count())
                .totalTeachers(teacherService.count())
                .totalClasses(classService.count())
                .activeEnrollments(enrollmentService.countByStatus(EnrollmentStatus.APPROVED))
                .totalRevenue(financeService.getTotalCollected())
                .totalPending(financeService.getTotalPending())
                .paidPayments(financeService.countPaid())
                .unpaidPayments(financeService.countUnpaid())
                .recentActivities(List.of())
                .topDebtors(getTopDebtors())
                .build();
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
