package com.api.educore.repository;

import com.api.educore.model.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    Optional<AcademicYear> findByActiveTrue();
    Optional<AcademicYear> findByName(String name);
}
