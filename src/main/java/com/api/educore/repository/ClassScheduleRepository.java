package com.api.educore.repository;

import com.api.educore.model.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findBySchoolClassId(Long schoolClassId);
    List<ClassSchedule> findByTeacherId(Long teacherId);
    List<ClassSchedule> findByDayOfWeek(String dayOfWeek);
    List<ClassSchedule> findBySchoolId(Long schoolId);
}
