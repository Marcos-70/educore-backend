package com.api.educore.service;

import com.api.educore.dto.ScheduleDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ClassScheduleRepository scheduleRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public List<ScheduleDTO> findByClass(Long classId) {
        return scheduleRepository.findBySchoolClassId(classId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ScheduleDTO> findByTeacher(Long teacherId) {
        return scheduleRepository.findByTeacherId(teacherId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ScheduleDTO> findAll() {
        return scheduleRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ScheduleDTO create(ScheduleDTO dto) {
        ClassSchedule schedule = new ClassSchedule();
        SchoolClass sc = classRepository.findById(dto.getSchoolClassId())
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
        schedule.setSchoolClass(sc);
        schedule.setSubject(subject);
        schedule.setTeacher(teacher);
        schedule.setDayOfWeek(dto.getDayOfWeek());
        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        schedule.setRoom(dto.getRoom());
        return toDTO(scheduleRepository.save(schedule));
    }

    public void delete(Long id) {
        scheduleRepository.deleteById(id);
    }

    private ScheduleDTO toDTO(ClassSchedule s) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(s.getId());
        dto.setSchoolClassId(s.getSchoolClass().getId());
        dto.setSchoolClassName(s.getSchoolClass().getName());
        dto.setSubjectId(s.getSubject().getId());
        dto.setSubjectName(s.getSubject().getName());
        dto.setTeacherId(s.getTeacher().getId());
        dto.setTeacherName(s.getTeacher().getName());
        dto.setDayOfWeek(s.getDayOfWeek());
        dto.setStartTime(s.getStartTime());
        dto.setEndTime(s.getEndTime());
        dto.setRoom(s.getRoom());
        return dto;
    }
}
