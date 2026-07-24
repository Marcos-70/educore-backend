package com.api.educore.repository;

import com.api.educore.model.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findBySchoolClassId(Long schoolClassId);
    List<Assessment> findByTrimesterId(Long trimesterId);
    List<Assessment> findBySchoolId(Long schoolId);
}
