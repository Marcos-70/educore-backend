package com.api.educore.dto;

import com.api.educore.model.LoanStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LibraryLoanDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long readerId;
    private String readerName;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;
    private int renewalCount;
    private String notes;
}
