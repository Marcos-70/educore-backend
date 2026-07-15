package com.api.educore.repository;

import com.api.educore.model.Bus;
import com.api.educore.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusRepository extends JpaRepository<Bus, Long> {
    List<Bus> findByStatus(Status status);
    List<Bus> findByPlateNumberContaining(String plateNumber);
    List<Bus> findBySchoolId(Long schoolId);
}
