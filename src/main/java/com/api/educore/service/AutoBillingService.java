package com.api.educore.service;

import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutoBillingService {

    private final PaymentRepository paymentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ServicePriceRepository servicePriceRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 1 1 * ?") // Run at 1 AM on the 1st of every month
    public void generateMonthlyInvoices() {
        log.info("Starting auto-billing for monthly invoices...");
        List<SchoolSettings> allSettings = schoolSettingsRepository.findAll();
        for (SchoolSettings settings : allSettings) {
            if (settings.isAutoBilling() && settings.getSchool() != null) {
                try {
                    generateInvoicesForSchool(settings.getSchool().getId());
                } catch (Exception e) {
                    log.error("Error generating invoices for school {}: {}", settings.getSchool().getName(), e.getMessage());
                }
            }
        }
        log.info("Auto-billing completed.");
    }

    public void generateInvoicesForSchool(Long schoolId) {
        List<Enrollment> activeEnrollments = enrollmentRepository.findBySchoolId(schoolId).stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.APPROVED || e.getStatus() == EnrollmentStatus.CONFIRMED)
                .toList();

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        for (Enrollment enrollment : activeEnrollments) {
            boolean alreadyPaid = paymentRepository.findBySchoolId(schoolId).stream()
                    .anyMatch(p -> p.getStudent() != null
                            && p.getStudent().getId().equals(enrollment.getStudent().getId())
                            && currentMonth.equals(p.getMonth())
                            && !p.isCancelled());

            if (!alreadyPaid) {
                List<ServicePrice> prices = servicePriceRepository.findBySchoolId(schoolId).stream()
                        .filter(sp -> sp.getCategory() == ServiceCategory.PROPINA && sp.isActive())
                        .toList();

                if (!prices.isEmpty()) {
                    ServicePrice price = prices.get(0);
                    String receipt = String.format("AUTO-%s-%d", currentMonth, enrollment.getId());

                    School school = userRepository.findBySchoolId(schoolId).stream()
                            .findFirst()
                            .map(User::getSchool)
                            .orElse(null);

                    Payment payment = Payment.builder()
                            .receiptNumber(receipt)
                            .student(enrollment.getStudent())
                            .paymentType(PaymentType.TUITION)
                            .paymentMethod(PaymentMethod.TRANSFER)
                            .amount(price.getPrice())
                            .finalAmount(price.getPrice())
                            .status(PaymentStatus.UNPAID)
                            .paymentDate(LocalDate.now())
                            .month(currentMonth)
                            .description("Propina mensal - " + currentMonth)
                            .school(school)
                            .build();
                    paymentRepository.save(payment);
                    log.info("Generated invoice {} for student {}", receipt, enrollment.getStudent().getId());
                }
            }
        }
    }
}
