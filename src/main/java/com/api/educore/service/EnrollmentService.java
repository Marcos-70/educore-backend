package com.api.educore.service;

import com.api.educore.dto.EnrollmentDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;

    public List<EnrollmentDTO> findAll() {
        return enrollmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> findByStatus(EnrollmentStatus status) {
        return enrollmentRepository.findByStatus(status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EnrollmentDTO create(EnrollmentDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));

        // Prevent duplicate enrollment in the same academic year
        String academicYear = dto.getAcademicYear();
        List<Enrollment> existingEnrollments = enrollmentRepository.findByStudentIdAndAcademicYear(dto.getStudentId(), academicYear);
        boolean alreadyEnrolled = existingEnrollments.stream()
                .anyMatch(e -> e.getStatus() != EnrollmentStatus.REJECTED && e.getStatus() != EnrollmentStatus.CANCELLED);
        if (alreadyEnrolled) {
            throw new RuntimeException("Este aluno já está matriculado/confirmado no ano letivo " + academicYear + ". Não é possível criar uma matrícula duplicada.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        if (dto.getClassId() != null) {
            SchoolClass sc = classRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
            long currentEnrolled = enrollmentRepository.findAll().stream()
                    .filter(e -> e.getSchoolClass() != null && e.getSchoolClass().getId().equals(sc.getId()))
                    .filter(e -> e.getStatus() != EnrollmentStatus.REJECTED && e.getStatus() != EnrollmentStatus.CANCELLED)
                    .count();
            if (currentEnrolled >= sc.getCapacity()) {
                throw new RuntimeException("Turma atingiu a capacidade máxima (" + sc.getCapacity() + " alunos). Não é possível matricular mais alunos.");
            }
            enrollment.setSchoolClass(sc);
        }
        enrollment.setAcademicYear(academicYear);

        // Determine status based on enrollmentType
        String enrollmentType = dto.getEnrollmentType() != null ? dto.getEnrollmentType() : "MATRICULA";
        enrollment.setEnrollmentType(enrollmentType);
        enrollment.setStatus(EnrollmentStatus.APPROVED);

        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setPhoto(dto.getPhoto());
        enrollment.setPreviousSchool(dto.getPreviousSchool());
        return toDTO(enrollmentRepository.save(enrollment));
    }

    public EnrollmentDTO update(Long id, EnrollmentDTO dto) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));
        if (dto.getClassId() != null) {
            SchoolClass sc = classRepository.findById(dto.getClassId()).orElse(null);
            enrollment.setSchoolClass(sc);
        }
        enrollment.setPreviousSchool(dto.getPreviousSchool());
        return toDTO(enrollmentRepository.save(enrollment));
    }

    public boolean isStudentEnrolledInYear(Long studentId, String academicYear) {
        return enrollmentRepository.findByStudentIdAndAcademicYear(studentId, academicYear)
                .stream()
                .anyMatch(e -> e.getStatus() != EnrollmentStatus.REJECTED && e.getStatus() != EnrollmentStatus.CANCELLED);
    }

    public EnrollmentDTO updateStatus(Long id, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula não encontrada"));
        enrollment.setStatus(status);
        return toDTO(enrollmentRepository.save(enrollment));
    }

    public void delete(Long id) {
        enrollmentRepository.deleteById(id);
    }

    public long count() {
        return enrollmentRepository.count();
    }

    public long countByStatus(EnrollmentStatus status) {
        return enrollmentRepository.countByStatus(status);
    }

    private EnrollmentDTO toDTO(Enrollment e) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(e.getId());
        dto.setStudentId(e.getStudent().getId());
        dto.setStudentName(e.getStudent().getFullName());
        dto.setClassId(e.getSchoolClass() != null ? e.getSchoolClass().getId() : null);
        dto.setClassName(e.getSchoolClass() != null ? e.getSchoolClass().getName() : null);
        dto.setAcademicYear(e.getAcademicYear());
        dto.setStatus(e.getStatus());
        dto.setEnrollmentType(e.getEnrollmentType());
        dto.setEnrollmentDate(e.getEnrollmentDate());
        dto.setPhoto(e.getPhoto());
        dto.setPreviousSchool(e.getPreviousSchool());
        return dto;
    }
}
