package com.api.educore.repository;

import com.api.educore.model.Trimester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrimesterRepository extends JpaRepository<Trimester, Long> {
    List<Trimester> findByAcademicYearId(Long academicYearId);
    List<Trimester> findByActiveTrue();
}
