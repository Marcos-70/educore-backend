package com.api.educore.repository;

import com.api.educore.model.Payment;
import com.api.educore.model.PaymentStatus;
import com.api.educore.model.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReceiptNumber(String receiptNumber);
    List<Payment> findByStudentId(Long studentId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByPaymentType(PaymentType type);
    List<Payment> findByPaymentDateBetween(LocalDate start, LocalDate end);
    List<Payment> findByCancelledFalse();
    List<Payment> findBySchoolId(Long schoolId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.cancelled = false")
    double sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = :status AND p.cancelled = false AND p.school.id = :schoolId")
    double sumAmountByStatusAndSchoolId(@Param("status") PaymentStatus status, @Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end AND p.cancelled = false")
    double sumAmountByPaymentDateBetweenAndCancelledFalse(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.cancelled = false AND p.school.id = :schoolId")
    long countByStatusAndSchoolId(@Param("status") PaymentStatus status, @Param("schoolId") Long schoolId);

    @Query("SELECT p.student.id, p.student.firstName, p.student.lastName, e.schoolClass.name, COUNT(p), SUM(p.amount) " +
            "FROM Payment p " +
            "LEFT JOIN Enrollment e ON e.student = p.student " +
            "WHERE p.status IN ('UNPAID', 'OVERDUE') AND p.cancelled = false AND p.school.id = :schoolId " +
            "GROUP BY p.student.id, p.student.firstName, p.student.lastName, e.schoolClass.name " +
            "ORDER BY SUM(p.amount) DESC")
    List<Object[]> findTopDebtorsBySchoolId(@Param("schoolId") Long schoolId);

    boolean existsByStudentIdAndMonthAndAcademicYearIdAndCancelledFalse(Long studentId, String month, Long academicYearId);

    boolean existsByStudentIdAndMonthAndCancelledFalse(Long studentId, String month);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.student.id = :studentId AND p.cancelled = false AND CONCAT(',', REPLACE(p.month, ' ', ''), ',') LIKE CONCAT('%,', REPLACE(:month, ' ', ''), ',%')")
    boolean existsByStudentIdAndMonthInField(@Param("studentId") Long studentId, @Param("month") String month);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p WHERE p.student.id = :studentId AND p.cancelled = false AND p.academicYearId = :academicYearId AND CONCAT(',', REPLACE(p.month, ' ', ''), ',') LIKE CONCAT('%,', REPLACE(:month, ' ', ''), ',%')")
    boolean existsByStudentIdAndMonthInFieldAndAcademicYearId(@Param("studentId") Long studentId, @Param("month") String month, @Param("academicYearId") Long academicYearId);

    @Query("SELECT p.month FROM Payment p WHERE p.student.id = :studentId AND p.cancelled = false AND p.academicYearId = :academicYearId AND p.month IS NOT NULL AND p.month <> ''")
    List<String> findMonthFieldsByStudentIdAndAcademicYearId(@Param("studentId") Long studentId, @Param("academicYearId") Long academicYearId);

    @Query("SELECT p.month FROM Payment p WHERE p.student.id = :studentId AND p.cancelled = false AND p.month IS NOT NULL AND p.month <> ''")
    List<String> findMonthFieldsByStudentId(@Param("studentId") Long studentId);

    List<Payment> findByStudentIdAndCancelledFalse(Long studentId);

    @Query("SELECT p FROM Payment p WHERE p.school.id = :schoolId ORDER BY p.createdAt DESC")
    List<Payment> findRecentBySchoolId(@Param("schoolId") Long schoolId);
}
