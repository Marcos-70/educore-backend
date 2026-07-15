package com.api.educore.repository;

import com.api.educore.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByAssessmentId(Long assessmentId);
    Optional<Grade> findByStudentIdAndAssessmentId(Long studentId, Long assessmentId);
    List<Grade> findByStudentIdAndAssessmentSchoolClassId(Long studentId, Long schoolClassId);
    List<Grade> findBySchoolId(Long schoolId);
}
