package com.api.educore.service;

import com.api.educore.dto.*;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicService {

    private final AcademicYearRepository academicYearRepository;
    private final TrimesterRepository trimesterRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    // Academic Years
    public List<AcademicYearDTO> findAllYears() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return academicYearRepository.findBySchoolId(school.getId()).stream()
                .map(this::toYearDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AcademicYearDTO createYear(AcademicYearDTO dto) {
        AcademicYear year = new AcademicYear();
        year.setName(dto.getName());
        year.setStartDate(dto.getStartDate());
        year.setEndDate(dto.getEndDate());
        year.setActive(dto.isActive());
        year.setSchool(getCurrentSchool());
        return toYearDTO(academicYearRepository.save(year));
    }

    @Transactional
    public AcademicYearDTO updateYear(Long id, AcademicYearDTO dto) {
        AcademicYear existing = academicYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ano letivo não encontrado"));
        existing.setName(dto.getName());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        return toYearDTO(academicYearRepository.save(existing));
    }

    @Transactional
    public void setActiveYear(Long id) {
        School school = getCurrentSchool();
        if (school != null) {
            academicYearRepository.findBySchoolId(school.getId())
                    .forEach(y -> { y.setActive(false); academicYearRepository.save(y); });
        }
        AcademicYear year = academicYearRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ano letivo não encontrado"));
        year.setActive(true);
        academicYearRepository.save(year);
    }

    public void deleteYear(Long id) {
        academicYearRepository.deleteById(id);
    }

    // Trimesters
    public List<TrimesterDTO> findTrimestersByYear(Long yearId) {
        return trimesterRepository.findByAcademicYearId(yearId).stream()
                .map(this::toTrimesterDTO).collect(Collectors.toList());
    }

    public TrimesterDTO createTrimester(TrimesterDTO dto) {
        Trimester t = new Trimester();
        t.setName(dto.getName());
        t.setSequenceNumber(dto.getSequenceNumber());
        t.setStartDate(dto.getStartDate());
        t.setEndDate(dto.getEndDate());
        t.setActive(dto.isActive());
        AcademicYear year = academicYearRepository.findById(dto.getAcademicYearId())
                .orElseThrow(() -> new RuntimeException("Ano letivo não encontrado"));
        t.setAcademicYear(year);
        t.setSchool(getCurrentSchool());
        return toTrimesterDTO(trimesterRepository.save(t));
    }

    public void deleteTrimester(Long id) {
        trimesterRepository.deleteById(id);
    }

    // Courses
    public List<CourseDTO> findAllCourses() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return courseRepository.findBySchoolId(school.getId()).stream()
                .map(this::toCourseDTO).collect(Collectors.toList());
    }

    public CourseDTO createCourse(CourseDTO dto) {
        Course c = new Course();
        mapCourse(dto, c);
        c.setSchool(getCurrentSchool());
        return toCourseDTO(courseRepository.save(c));
    }

    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Curso não encontrado"));
        mapCourse(dto, existing);
        return toCourseDTO(courseRepository.save(existing));
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    // Subjects
    public List<SubjectDTO> findAllSubjects() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return subjectRepository.findBySchoolId(school.getId()).stream()
                .map(this::toSubjectDTO).collect(Collectors.toList());
    }

    public SubjectDTO createSubject(SubjectDTO dto) {
        Subject s = new Subject();
        mapSubject(dto, s);
        s.setSchool(getCurrentSchool());
        return toSubjectDTO(subjectRepository.save(s));
    }

    public SubjectDTO updateSubject(Long id, SubjectDTO dto) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
        mapSubject(dto, existing);
        return toSubjectDTO(subjectRepository.save(existing));
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    // Calendar Events
    public List<CalendarEventDTO> findAllEvents() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return calendarEventRepository.findBySchoolId(school.getId()).stream()
                .map(this::toEventDTO).collect(Collectors.toList());
    }

    public CalendarEventDTO createEvent(CalendarEventDTO dto) {
        CalendarEvent e = new CalendarEvent();
        e.setName(dto.getName());
        e.setStartDate(dto.getStartDate());
        e.setEndDate(dto.getEndDate());
        e.setType(dto.getType());
        e.setDescription(dto.getDescription());
        e.setSchool(getCurrentSchool());
        if (dto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(dto.getAcademicYearId()).orElse(null);
            e.setAcademicYear(year);
        }
        return toEventDTO(calendarEventRepository.save(e));
    }

    public CalendarEventDTO updateEvent(Long id, CalendarEventDTO dto) {
        CalendarEvent existing = calendarEventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
        existing.setName(dto.getName());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setType(dto.getType());
        existing.setDescription(dto.getDescription());
        if (dto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(dto.getAcademicYearId()).orElse(null);
            existing.setAcademicYear(year);
        }
        return toEventDTO(calendarEventRepository.save(existing));
    }

    public void deleteEvent(Long id) {
        calendarEventRepository.deleteById(id);
    }

    // Mapping helpers
    private void mapCourse(CourseDTO dto, Course c) {
        c.setCode(dto.getCode());
        c.setName(dto.getName());
        c.setAbbrevName(dto.getAbbrevName());
        c.setDescription(dto.getDescription());
        c.setLevel(dto.getLevel());
        c.setArea(dto.getArea());
        c.setDepartment(dto.getDepartment());
        c.setDuration(dto.getDuration());
        c.setSemesters(dto.getSemesters());
        c.setCredits(dto.getCredits());
        c.setTotalHours(dto.getTotalHours());
        c.setCoordinator(dto.getCoordinator());
        c.setCoordinatorContact(dto.getCoordinatorContact());
        c.setActive(dto.isActive());
    }

    private void mapSubject(SubjectDTO dto, Subject s) {
        s.setCode(dto.getCode());
        s.setName(dto.getName());
        s.setAbbrevName(dto.getAbbrevName());
        s.setDescription(dto.getDescription());
        s.setClassLevel(dto.getClassLevel());
        s.setType(dto.getType());
        s.setMinimumGrade(dto.getMinimumGrade());
        s.setMinimumAttendance(dto.getMinimumAttendance());
        s.setActive(dto.isActive());
    }

    private AcademicYearDTO toYearDTO(AcademicYear y) {
        AcademicYearDTO dto = new AcademicYearDTO();
        dto.setId(y.getId());
        dto.setName(y.getName());
        dto.setStartDate(y.getStartDate());
        dto.setEndDate(y.getEndDate());
        dto.setActive(y.isActive());
        if (y.getTrimesters() != null) {
            dto.setTrimesters(y.getTrimesters().stream().map(this::toTrimesterDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private TrimesterDTO toTrimesterDTO(Trimester t) {
        TrimesterDTO dto = new TrimesterDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setSequenceNumber(t.getSequenceNumber());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setActive(t.isActive());
        dto.setAcademicYearId(t.getAcademicYear() != null ? t.getAcademicYear().getId() : null);
        return dto;
    }

    private CourseDTO toCourseDTO(Course c) {
        CourseDTO dto = new CourseDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setName(c.getName());
        dto.setAbbrevName(c.getAbbrevName());
        dto.setDescription(c.getDescription());
        dto.setLevel(c.getLevel());
        dto.setArea(c.getArea());
        dto.setDepartment(c.getDepartment());
        dto.setDuration(c.getDuration());
        dto.setSemesters(c.getSemesters());
        dto.setCredits(c.getCredits());
        dto.setTotalHours(c.getTotalHours());
        dto.setCoordinator(c.getCoordinator());
        dto.setCoordinatorContact(c.getCoordinatorContact());
        dto.setActive(c.isActive());
        return dto;
    }

    private SubjectDTO toSubjectDTO(Subject s) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(s.getId());
        dto.setCode(s.getCode());
        dto.setName(s.getName());
        dto.setAbbrevName(s.getAbbrevName());
        dto.setDescription(s.getDescription());
        dto.setClassLevel(s.getClassLevel());
        dto.setType(s.getType());
        dto.setMinimumGrade(s.getMinimumGrade());
        dto.setMinimumAttendance(s.getMinimumAttendance());
        dto.setActive(s.isActive());
        return dto;
    }

    private CalendarEventDTO toEventDTO(CalendarEvent e) {
        CalendarEventDTO dto = new CalendarEventDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setStartDate(e.getStartDate());
        dto.setEndDate(e.getEndDate());
        dto.setType(e.getType());
        dto.setDescription(e.getDescription());
        dto.setAcademicYearId(e.getAcademicYear() != null ? e.getAcademicYear().getId() : null);
        return dto;
    }
}
