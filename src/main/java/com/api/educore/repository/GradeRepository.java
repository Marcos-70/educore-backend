package com.api.educore.repository;

import com.api.educore.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByAssessmentId(Long assessmentId);
    Optional<Grade> findByStudentIdAndAssessmentId(Long studentId, Long assessmentId);
    List<Grade> findByStudentIdAndAssessmentSchoolClassId(Long studentId, Long schoolClassId);
    List<Grade> findBySchoolId(Long schoolId);

    @Query("SELECT g FROM Grade g JOIN g.assessment a WHERE g.student.id = :studentId AND a.schoolClass.id = :classId AND a.trimester.id = :trimesterId")
    List<Grade> findByStudentAndClassAndTrimester(@Param("studentId") Long studentId, @Param("classId") Long classId, @Param("trimesterId") Long trimesterId);

    @Query("SELECT g FROM Grade g JOIN g.student s WHERE g.assessment.id = :assessmentId ORDER BY s.firstName, s.lastName")
    List<Grade> findByAssessmentOrderedByStudent(@Param("assessmentId") Long assessmentId);
}
