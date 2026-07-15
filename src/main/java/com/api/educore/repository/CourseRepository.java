package com.api.educore.repository;

import com.api.educore.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByActiveTrue();
    List<Course> findByNameContainingIgnoreCase(String name);
    List<Course> findBySchoolId(Long schoolId);
}
