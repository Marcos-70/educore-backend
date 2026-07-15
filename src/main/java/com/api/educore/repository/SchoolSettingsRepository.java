package com.api.educore.repository;

import com.api.educore.model.SchoolSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, Long> {
    Optional<SchoolSettings> findBySchoolId(Long schoolId);
}
