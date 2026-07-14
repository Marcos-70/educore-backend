package com.api.educore.service;

import com.api.educore.dto.ClassDTO;
import com.api.educore.model.SchoolClass;
import com.api.educore.model.Teacher;
import com.api.educore.repository.SchoolClassRepository;
import com.api.educore.repository.TeacherRepository;
import com.api.educore.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final SchoolClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    private final EnrollmentRepository enrollmentRepository;

    public List<ClassDTO> findAll() {
        return classRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ClassDTO findById(Long id) {
        SchoolClass sc = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        return toDTO(sc);
    }

    public ClassDTO create(ClassDTO dto) {
        SchoolClass sc = toEntity(dto);
        return toDTO(classRepository.save(sc));
    }

    public ClassDTO update(Long id, ClassDTO dto) {
        SchoolClass existing = classRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        existing.setName(dto.getName());
        existing.setClassLevel(dto.getClassLevel());
        existing.setCourse(dto.getCourse());
        existing.setRoom(dto.getRoom());
        existing.setCapacity(dto.getCapacity());
        existing.setShift(dto.getShift());
        existing.setAcademicYear(dto.getAcademicYear());
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElse(null);
            existing.setTeacher(teacher);
        }
        return toDTO(classRepository.save(existing));
    }

    public void delete(Long id) {
        classRepository.deleteById(id);
    }

    public long count() {
        return classRepository.count();
    }

    private ClassDTO toDTO(SchoolClass sc) {
        ClassDTO dto = new ClassDTO();
        dto.setId(sc.getId());
        dto.setName(sc.getName());
        dto.setClassLevel(sc.getClassLevel());
        dto.setCourse(sc.getCourse());
        dto.setTeacherId(sc.getTeacher() != null ? sc.getTeacher().getId() : null);
        dto.setTeacherName(sc.getTeacher() != null ? sc.getTeacher().getName() : null);
        dto.setRoom(sc.getRoom());
        dto.setCapacity(sc.getCapacity());
        dto.setShift(sc.getShift());
        dto.setAcademicYear(sc.getAcademicYear());
        long enrolled = enrollmentRepository.findAll().stream()
                .filter(e -> e.getSchoolClass() != null && e.getSchoolClass().getId().equals(sc.getId()))
                .count();
        dto.setEnrolledCount((int) enrolled);
        return dto;
    }

    private SchoolClass toEntity(ClassDTO dto) {
        SchoolClass sc = new SchoolClass();
        sc.setName(dto.getName());
        sc.setClassLevel(dto.getClassLevel());
        sc.setCourse(dto.getCourse());
        sc.setRoom(dto.getRoom());
        sc.setCapacity(dto.getCapacity());
        sc.setShift(dto.getShift());
        sc.setAcademicYear(dto.getAcademicYear());
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId()).orElse(null);
            sc.setTeacher(teacher);
        }
        return sc;
    }
}
