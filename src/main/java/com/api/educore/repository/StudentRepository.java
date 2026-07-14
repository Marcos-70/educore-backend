package com.api.educore.repository;

import com.api.educore.model.Student;
import com.api.educore.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNumber(String studentNumber);
    List<Student> findByStatus(Status status);
    List<Student> findByFirstNameContainingOrLastNameContaining(String firstName, String lastName);
    long countByStatus(Status status);
}
