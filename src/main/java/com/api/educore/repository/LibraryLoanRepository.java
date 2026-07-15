package com.api.educore.repository;

import com.api.educore.model.LibraryLoan;
import com.api.educore.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryLoanRepository extends JpaRepository<LibraryLoan, Long> {
    List<LibraryLoan> findByStatus(LoanStatus status);
    List<LibraryLoan> findByReaderId(Long readerId);
    List<LibraryLoan> findByBookId(Long bookId);
    long countByReaderIdAndStatus(Long readerId, LoanStatus status);
    List<LibraryLoan> findBySchoolId(Long schoolId);
}
