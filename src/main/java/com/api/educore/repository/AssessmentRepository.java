package com.api.educore.repository;

import com.api.educore.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findBySchoolClassIdAndSubjectId(Long schoolClassId, Long subjectId);
    List<Assessment> findBySchoolClassIdAndSubjectIdAndTrimesterId(Long schoolClassId, Long subjectId, Long trimesterId);
    List<Assessment> findBySchoolClassId(Long schoolClassId);
    List<Assessment> findByTrimesterId(Long trimesterId);
    List<Assessment> findBySchoolId(Long schoolId);
    boolean existsBySchoolClassIdAndSubjectIdAndTrimesterIdAndNameIgnoreCase(Long schoolClassId, Long subjectId, Long trimesterId, String name);

    @Query("SELECT a FROM Assessment a LEFT JOIN FETCH a.trimester LEFT JOIN FETCH a.subject WHERE a.schoolClass.id = :classId")
    List<Assessment> findBySchoolClassIdWithTrimesterAndSubject(@Param("classId") Long classId);
}
