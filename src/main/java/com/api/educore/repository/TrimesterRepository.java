package com.api.educore.repository;

import com.api.educore.model.Trimester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrimesterRepository extends JpaRepository<Trimester, Long> {
    List<Trimester> findByAcademicYearId(Long academicYearId);
    List<Trimester> findByActiveTrue();

    @Query("SELECT t FROM Trimester t LEFT JOIN FETCH t.academicYear WHERE t.id = :id")
    Optional<Trimester> findByIdWithAcademicYear(@Param("id") Long id);
}
