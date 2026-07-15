package com.api.educore.repository;

import com.api.educore.model.Status;
import com.api.educore.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    List<Teacher> findByStatus(Status status);
    List<Teacher> findByNameContainingIgnoreCase(String name);
    long countByStatus(Status status);
    List<Teacher> findBySchoolId(Long schoolId);
}
