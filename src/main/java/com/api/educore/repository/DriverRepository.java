package com.api.educore.repository;

import com.api.educore.model.Driver;
import com.api.educore.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findByStatus(Status status);
    List<Driver> findByNameContainingIgnoreCase(String name);
    List<Driver> findBySchoolId(Long schoolId);
}
