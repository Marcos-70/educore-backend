package com.api.educore.repository;

import com.api.educore.model.ClassLevel;
import com.api.educore.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByClassLevel(ClassLevel classLevel);
    List<Subject> findByActiveTrue();
    List<Subject> findByNameContainingIgnoreCase(String name);
    List<Subject> findBySchoolId(Long schoolId);
}
