package com.api.educore.repository;

import com.api.educore.model.Attendance;
import com.api.educore.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findBySchoolClassIdAndDate(Long schoolClassId, LocalDate date);
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByStudentIdAndDateBetween(Long studentId, LocalDate start, LocalDate end);
    long countBySchoolClassIdAndDateAndStatus(Long schoolClassId, LocalDate date, AttendanceStatus status);
    List<Attendance> findBySchoolId(Long schoolId);
}
