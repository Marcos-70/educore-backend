package com.api.educore.repository;

import com.api.educore.model.Enrollment;
import com.api.educore.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStatus(EnrollmentStatus status);
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByAcademicYear(String academicYear);
    List<Enrollment> findByStudentIdAndAcademicYear(Long studentId, String academicYear);
    long countByStatus(EnrollmentStatus status);
    List<Enrollment> findBySchoolId(Long schoolId);
    List<Enrollment> findBySchoolClassId(Long schoolClassId);

    @Query("SELECT e FROM Enrollment e LEFT JOIN FETCH e.student WHERE e.schoolClass.id = :classId")
    List<Enrollment> findBySchoolClassIdWithStudent(@Param("classId") Long classId);
}
