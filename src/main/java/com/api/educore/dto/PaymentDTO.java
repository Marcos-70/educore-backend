package com.api.educore.dto;

import com.api.educore.model.PaymentStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    private Long id;
    private String receiptNumber;
    private Long studentId;
    private String studentName;
    private String paymentType;
    private String paymentMethod;
    private double amount;
    private double discount;
    private double fine;
    private double finalAmount;
    private PaymentStatus status;
    private LocalDate paymentDate;
    private String description;
    private String reference;
    private String month;
    private Long academicYearId;
    private String classLevel;
    private double cashReceived;
    private double change;
    private String cashRegisterName;
    private String cardIssuer;
    private String cardType;
    private String transactionReference;
    private String bankOrigin;
    private String bankDestination;
    private String depositNumber;
    private String mcxReference;
    private String mcxTransactionNumber;
    private String referenceEntity;
    private String referenceNumber;
    private boolean cancelled;
    private String cancellationReason;
    private String cancellationObservation;
    private LocalDateTime cancelledAt;

    // School data for invoice/receipt
    private String schoolName;
    private String schoolAddress;
    private String schoolNif;
    private String schoolPhone;
    private String schoolEmail;
}
