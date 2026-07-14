package com.api.educore.repository;

import com.api.educore.model.CalendarEvent;
import com.api.educore.model.CalendarEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByAcademicYearId(Long academicYearId);
    List<CalendarEvent> findByType(CalendarEventType type);
}
