package com.api.educore.service;

import com.api.educore.dto.PaymentDTO;
import com.api.educore.model.*;
import com.api.educore.repository.PaymentRepository;
import com.api.educore.repository.SchoolSettingsRepository;
import com.api.educore.repository.ServicePriceRepository;
import com.api.educore.repository.StudentRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final ServicePriceRepository servicePriceRepository;
    private final SchoolSettingsRepository schoolSettingsRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    private static final Map<String, ServiceCategory> PAYMENT_TYPE_TO_CATEGORY = new HashMap<>();
    static {
        PAYMENT_TYPE_TO_CATEGORY.put("Propina", ServiceCategory.PROPINA);
        PAYMENT_TYPE_TO_CATEGORY.put("Propina/Mensalidade", ServiceCategory.PROPINA);
        PAYMENT_TYPE_TO_CATEGORY.put("Matricula/Confirmacao", ServiceCategory.MATRICULA);
        PAYMENT_TYPE_TO_CATEGORY.put("Matricula", ServiceCategory.MATRICULA);
        PAYMENT_TYPE_TO_CATEGORY.put("Transporte", ServiceCategory.TRANSPORTE);
        PAYMENT_TYPE_TO_CATEGORY.put("Uniforme", ServiceCategory.UNIFORME);
        PAYMENT_TYPE_TO_CATEGORY.put("Material escolar", ServiceCategory.MATERIAL);
        PAYMENT_TYPE_TO_CATEGORY.put("Material Escolar", ServiceCategory.MATERIAL);
        PAYMENT_TYPE_TO_CATEGORY.put("Emissao de cartao", ServiceCategory.CARTAO);
        PAYMENT_TYPE_TO_CATEGORY.put("Emissao de Cartao", ServiceCategory.CARTAO);
        PAYMENT_TYPE_TO_CATEGORY.put("Certificados", ServiceCategory.CERTIFICADO);
        PAYMENT_TYPE_TO_CATEGORY.put("Declaracoes", ServiceCategory.DECLARACAO);
    }

    public List<PaymentDTO> findAll() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return paymentRepository.findBySchoolId(school.getId()).stream()
                .filter(p -> !p.isCancelled())
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<PaymentDTO> findByStatus(PaymentStatus status) {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return paymentRepository.findBySchoolId(school.getId()).stream()
                .filter(p -> p.getStatus() == status && !p.isCancelled())
                .map(this::toDTO).collect(Collectors.toList());
    }

    public PaymentDTO findById(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento nao encontrado"));
        return toDTO(p);
    }

    public PaymentDTO create(PaymentDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Aluno nao encontrado"));

        if (dto.getMonth() != null && !dto.getMonth().isBlank()) {
            String[] months = dto.getMonth().split(",");
            for (String m : months) {
                String month = m.trim();
                if (!month.isEmpty()) {
                    boolean alreadyPaid = paymentRepository.existsByStudentIdAndMonthInField(
                            dto.getStudentId(), month);
                    if (alreadyPaid) {
                        throw new RuntimeException("O mes " + month + " ja foi pago para este aluno.");
                    }
                }
            }
        }

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setSchool(getCurrentSchool());

        payment.setReceiptNumber(generateSequentialReceiptNumber("FAT-"));

        PaymentType paymentType = mapPaymentType(dto.getPaymentType());
        payment.setPaymentType(paymentType);

        PaymentMethod paymentMethod = mapPaymentMethod(dto.getPaymentMethod());
        payment.setPaymentMethod(paymentMethod);

        payment.setAmount(dto.getAmount());
        payment.setDiscount(dto.getDiscount());
        payment.setFine(dto.getFine());
        payment.setFinalAmount(dto.getAmount() - dto.getDiscount() + dto.getFine());
        payment.setPaymentDate(LocalDate.now());
        payment.setDescription(dto.getDescription());
        payment.setReference(dto.getReference());
        payment.setMonth(dto.getMonth());
        payment.setAcademicYearId(dto.getAcademicYearId());
        payment.setStatus(PaymentStatus.PAID);

        payment.setCashReceived(dto.getCashReceived());
        payment.setChange(dto.getChange());
        payment.setCashRegisterName(dto.getCashRegisterName());
        payment.setCardIssuer(dto.getCardIssuer());
        payment.setCardType(dto.getCardType());
        payment.setTransactionReference(dto.getTransactionReference());
        payment.setBankOrigin(dto.getBankOrigin());
        payment.setBankDestination(dto.getBankDestination());
        payment.setDepositNumber(dto.getDepositNumber());
        payment.setMcxReference(dto.getMcxReference());
        payment.setMcxTransactionNumber(dto.getMcxTransactionNumber());
        payment.setReferenceEntity(dto.getReferenceEntity());
        payment.setReferenceNumber(dto.getReferenceNumber());

        return toDTO(paymentRepository.save(payment));
    }

    public PaymentDTO cancel(Long id, String reason, String observation) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento nao encontrado"));

        String creditNoteNumber = generateSequentialReceiptNumber("NC-");

        payment.setCancelled(true);
        payment.setCancellationReason(reason);
        payment.setCancellationObservation(observation);
        payment.setCancelledAt(LocalDateTime.now());
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setReceiptNumber(creditNoteNumber);

        return toDTO(paymentRepository.save(payment));
    }

    public double getTotalCollected() {
        School school = getCurrentSchool();
        if (school == null) return 0.0;
        return paymentRepository.sumAmountByStatusAndSchoolId(PaymentStatus.PAID, school.getId());
    }

    public double getTotalPending() {
        School school = getCurrentSchool();
        if (school == null) return 0.0;
        return paymentRepository.sumAmountByStatusAndSchoolId(PaymentStatus.UNPAID, school.getId());
    }

    public long countPaid() {
        School school = getCurrentSchool();
        if (school == null) return 0;
        return paymentRepository.countByStatusAndSchoolId(PaymentStatus.PAID, school.getId());
    }

    public long countUnpaid() {
        School school = getCurrentSchool();
        if (school == null) return 0;
        return paymentRepository.countByStatusAndSchoolId(PaymentStatus.UNPAID, school.getId());
    }

    private double lookupServicePrice(String paymentType, String classLevel) {
        ServiceCategory category = PAYMENT_TYPE_TO_CATEGORY.get(paymentType);
        if (category == null) {
            return 0.0;
        }

        if (classLevel != null && !classLevel.isBlank()) {
            try {
                ClassLevel level = ClassLevel.valueOf(classLevel.toUpperCase());
                return servicePriceRepository.findFirstByCategoryAndClassLevelAndActiveTrueOrderByPriceDesc(category, level)
                        .map(ServicePrice::getPrice)
                        .orElseGet(() -> {
                            return servicePriceRepository.findFirstByCategoryAndClassLevelIsNullAndActiveTrueOrderByPriceDesc(category)
                                    .map(ServicePrice::getPrice)
                                    .orElse(0.0);
                        });
            } catch (IllegalArgumentException ignored) {}
        }

        return servicePriceRepository.findFirstByCategoryAndClassLevelIsNullAndActiveTrueOrderByPriceDesc(category)
                .orElseGet(() -> servicePriceRepository.findFirstByCategoryAndActiveTrueOrderByPriceDesc(category)
                        .orElse(null))
                .getPrice();
    }

    private double calculateFine(String paymentType, String classLevel, String month, double amount) {
        ServiceCategory category = PAYMENT_TYPE_TO_CATEGORY.get(paymentType);
        if (category == null) return 0.0;

        ServicePrice servicePrice = null;
        if (classLevel != null && !classLevel.isBlank()) {
            try {
                ClassLevel level = ClassLevel.valueOf(classLevel.toUpperCase());
                servicePrice = servicePriceRepository.findFirstByCategoryAndClassLevelAndActiveTrueOrderByPriceDesc(category, level)
                        .orElseGet(() -> servicePriceRepository.findFirstByCategoryAndClassLevelIsNullAndActiveTrueOrderByPriceDesc(category)
                                .orElse(null));
            } catch (IllegalArgumentException ignored) {
                servicePrice = servicePriceRepository.findFirstByCategoryAndClassLevelIsNullAndActiveTrueOrderByPriceDesc(category)
                        .orElse(null);
            }
        } else {
            servicePrice = servicePriceRepository.findFirstByCategoryAndClassLevelIsNullAndActiveTrueOrderByPriceDesc(category)
                    .orElse(null);
        }

        if (servicePrice == null || servicePrice.getDueDay() == null) return 0.0;
        if (servicePrice.getFinePercent1() == null && servicePrice.getFinePercent2() == null && servicePrice.getFinePercent3() == null) return 0.0;

        int dueDay = servicePrice.getDueDay();
        int fineDay2 = servicePrice.getFineDay2() != null ? servicePrice.getFineDay2() : 25;
        int fineDay3 = servicePrice.getFineDay3() != null ? servicePrice.getFineDay3() : 31;
        double finePct1 = servicePrice.getFinePercent1() != null ? servicePrice.getFinePercent1() : 0;
        double finePct2 = servicePrice.getFinePercent2() != null ? servicePrice.getFinePercent2() : 0;
        double finePct3 = servicePrice.getFinePercent3() != null ? servicePrice.getFinePercent3() : 0;

        LocalDate monthDate = parseMonthToDate(month, 1);
        if (monthDate == null) return 0.0;
        int monthNumber = monthDate.getMonthValue();
        int monthYear = monthDate.getYear();

        LocalDate today = LocalDate.now();
        int todayDay = today.getDayOfMonth();
        int todayMonth = today.getMonthValue();
        int todayYear = today.getYear();

        double finePercent = 0.0;

        if (todayMonth == monthNumber && todayYear == monthYear) {
            if (todayDay <= dueDay) {
                return 0.0;
            } else if (todayDay <= fineDay2) {
                finePercent = finePct1;
            } else if (todayDay <= fineDay3) {
                finePercent = finePct2;
            } else {
                finePercent = finePct3;
            }
        }
        else if (todayYear > monthYear || (todayYear == monthYear && todayMonth > monthNumber)) {
            finePercent = finePct3;
        }

        return amount * finePercent / 100.0;
    }

    private void checkSequentialPayment(Long studentId, String month) {
        String[] allMonths = {"Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

        int requestedMonthIdx = -1;
        for (int i = 0; i < allMonths.length; i++) {
            if (allMonths[i].equalsIgnoreCase(month)) {
                requestedMonthIdx = i;
                break;
            }
        }
        if (requestedMonthIdx <= 0) return;

        for (int i = 0; i < requestedMonthIdx; i++) {
            String prevMonth = allMonths[i];
            boolean paid = paymentRepository.existsByStudentIdAndMonthAndCancelledFalse(
                    studentId, prevMonth);
            if (!paid) {
                throw new RuntimeException("Nao e possivel pagar " + month + " sem pagar primeiro " + prevMonth + ". Pagamento sequencial obrigatorio.");
            }
        }
    }

    private String generateSequentialReceiptNumber(String prefix) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fullPrefix = prefix + dateStr + "-";

        School school = getCurrentSchool();
        List<Payment> todayPayments = (school != null ? paymentRepository.findBySchoolId(school.getId()) : List.of()).stream()
                .filter(p -> p.getReceiptNumber() != null && p.getReceiptNumber().startsWith(fullPrefix))
                .sorted((a, b) -> b.getReceiptNumber().compareTo(a.getReceiptNumber()))
                .collect(Collectors.toList());

        int nextNumber = 1;
        if (!todayPayments.isEmpty()) {
            String lastNum = todayPayments.get(0).getReceiptNumber().substring(fullPrefix.length());
            try {
                nextNumber = Integer.parseInt(lastNum) + 1;
            } catch (NumberFormatException ignored) {}
        }

        return fullPrefix + String.format("%03d", nextNumber);
    }

    private LocalDate parseMonthToDate(String month, int dueDay) {
        if (month == null || month.isBlank()) return null;
        int currentYear = LocalDate.now().getYear();
        return switch (month.toLowerCase()) {
            case "janeiro" -> LocalDate.of(currentYear, 1, Math.min(dueDay, 28));
            case "fevereiro" -> LocalDate.of(currentYear, 2, Math.min(dueDay, 28));
            case "marco", "março" -> LocalDate.of(currentYear, 3, Math.min(dueDay, 31));
            case "abril" -> LocalDate.of(currentYear, 4, Math.min(dueDay, 30));
            case "maio" -> LocalDate.of(currentYear, 5, Math.min(dueDay, 31));
            case "junho" -> LocalDate.of(currentYear, 6, Math.min(dueDay, 30));
            case "julho" -> LocalDate.of(currentYear, 7, Math.min(dueDay, 31));
            case "agosto" -> LocalDate.of(currentYear, 8, Math.min(dueDay, 31));
            case "setembro" -> LocalDate.of(currentYear, 9, Math.min(dueDay, 30));
            case "outubro" -> LocalDate.of(currentYear, 10, Math.min(dueDay, 31));
            case "novembro" -> LocalDate.of(currentYear, 11, Math.min(dueDay, 30));
            case "dezembro" -> LocalDate.of(currentYear, 12, Math.min(dueDay, 31));
            default -> null;
        };
    }

    private PaymentType mapPaymentType(String type) {
        if (type == null) return PaymentType.OTHER;
        return switch (type.toLowerCase()) {
            case "propina", "propina/mensalidade" -> PaymentType.TUITION;
            case "transporte" -> PaymentType.TRANSPORT;
            case "matricula/confirmacao", "matricula" -> PaymentType.REGISTRATION;
            case "certificados" -> PaymentType.CERTIFICATE;
            case "exame", "exames" -> PaymentType.EXAM;
            case "biblioteca" -> PaymentType.LIBRARY;
            case "seguro" -> PaymentType.INSURANCE;
            default -> PaymentType.OTHER;
        };
    }

    private PaymentMethod mapPaymentMethod(String method) {
        if (method == null) return PaymentMethod.CASH;
        return switch (method.toLowerCase()) {
            case "numerario", "numerário" -> PaymentMethod.CASH;
            case "cartao", "cartão" -> PaymentMethod.CARD;
            case "transferencia", "transferência" -> PaymentMethod.TRANSFER;
            case "deposito", "depósito" -> PaymentMethod.DEPOSIT;
            case "multicaixa express" -> PaymentMethod.MCX;
            case "referencia", "referência" -> PaymentMethod.REFERENCE;
            case "pagamento misto" -> PaymentMethod.MIXED;
            default -> PaymentMethod.CASH;
        };
    }

    private PaymentDTO toDTO(Payment p) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setReceiptNumber(p.getReceiptNumber());
        dto.setStudentId(p.getStudent().getId());
        dto.setStudentName(p.getStudent().getFullName());
        dto.setPaymentType(p.getPaymentType() != null ? p.getPaymentType().name() : null);
        dto.setPaymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null);
        dto.setAmount(p.getAmount());
        dto.setDiscount(p.getDiscount());
        dto.setFine(p.getFine());
        dto.setFinalAmount(p.getFinalAmount());
        dto.setStatus(p.getStatus());
        dto.setPaymentDate(p.getPaymentDate());
        dto.setDescription(p.getDescription());
        dto.setReference(p.getReference());
        dto.setMonth(p.getMonth());
        dto.setAcademicYearId(p.getAcademicYearId());
        dto.setCashReceived(p.getCashReceived());
        dto.setChange(p.getChange());
        dto.setCashRegisterName(p.getCashRegisterName());
        dto.setCardIssuer(p.getCardIssuer());
        dto.setCardType(p.getCardType());
        dto.setTransactionReference(p.getTransactionReference());
        dto.setBankOrigin(p.getBankOrigin());
        dto.setBankDestination(p.getBankDestination());
        dto.setDepositNumber(p.getDepositNumber());
        dto.setMcxReference(p.getMcxReference());
        dto.setMcxTransactionNumber(p.getMcxTransactionNumber());
        dto.setReferenceEntity(p.getReferenceEntity());
        dto.setReferenceNumber(p.getReferenceNumber());
        dto.setCancelled(p.isCancelled());
        dto.setCancellationReason(p.getCancellationReason());
        dto.setCancellationObservation(p.getCancellationObservation());
        dto.setCancelledAt(p.getCancelledAt());

        School school = getCurrentSchool();
        if (school != null) {
            schoolSettingsRepository.findBySchoolId(school.getId()).ifPresent(settings -> {
                dto.setSchoolName(settings.getSchoolName());
                dto.setSchoolAddress(settings.getAddress());
                dto.setSchoolNif(settings.getNif());
                dto.setSchoolPhone(settings.getPhone());
                dto.setSchoolEmail(settings.getEmail());
            });
        }

        return dto;
    }
}
