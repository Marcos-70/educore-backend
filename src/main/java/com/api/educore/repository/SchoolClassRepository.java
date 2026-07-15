package com.api.educore.repository;

import com.api.educore.model.ClassLevel;
import com.api.educore.model.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findByClassLevel(ClassLevel classLevel);
    List<SchoolClass> findByAcademicYear(String academicYear);
    List<SchoolClass> findByTeacherId(Long teacherId);
    List<SchoolClass> findBySchoolId(Long schoolId);
}
