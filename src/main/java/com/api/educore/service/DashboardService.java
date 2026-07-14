package com.api.educore.service;

import com.api.educore.dto.DashboardDTO;
import com.api.educore.model.EnrollmentStatus;
import com.api.educore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
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
        List<Object[]> results = paymentRepository.findTopDebtors();
        return results.stream()
                .limit(10)
                .map(row -> new DashboardDTO.TopDebtor(
                        row[1] + " " + row[2],  // studentName (firstName + lastName)
                        row[3] != null ? row[3].toString() : "N/A",  // className
                        ((Number) row[4]).intValue(),  // monthsOverdue (count of unpaid payments)
                        ((Number) row[5]).doubleValue()  // amount (sum of unpaid payments)
                ))
                .collect(Collectors.toList());
    }
}
