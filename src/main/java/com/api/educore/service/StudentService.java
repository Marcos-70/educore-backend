package com.api.educore.service;

import com.api.educore.dto.StudentDTO;
import com.api.educore.model.Student;
import com.api.educore.model.Status;
import com.api.educore.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<StudentDTO> findAll() {
        return studentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public StudentDTO findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        return toDTO(student);
    }

    public List<StudentDTO> search(String term) {
        return studentRepository.findByFirstNameContainingOrLastNameContaining(term, term)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<StudentDTO> findByStatus(Status status) {
        return studentRepository.findByStatus(status)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public StudentDTO create(StudentDTO dto) {
        Student student = toEntity(dto);
        student.setStudentNumber("EDU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return toDTO(studentRepository.save(student));
    }

    public StudentDTO update(Long id, StudentDTO dto) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        existing.setFirstName(dto.getFirstName());
        existing.setLastName(dto.getLastName());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setGender(dto.getGender());
        existing.setNationality(dto.getNationality());
        existing.setNif(dto.getNif());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setAddress(dto.getAddress());
        existing.setPhoto(dto.getPhoto());
        existing.setGuardianName(dto.getGuardianName());
        existing.setGuardianContact(dto.getGuardianContact());
        existing.setGuardianEmail(dto.getGuardianEmail());
        existing.setRelationship(dto.getRelationship());
        existing.setPreviousSchool(dto.getPreviousSchool());
        existing.setEmergencyContact(dto.getEmergencyContact());
        existing.setEmergencyPhone(dto.getEmergencyPhone());
        existing.setAllergies(dto.getAllergies());
        existing.setMedicalNotes(dto.getMedicalNotes());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        return toDTO(studentRepository.save(existing));
    }

    public void delete(Long id) {
        studentRepository.deleteById(id);
    }

    public long count() {
        return studentRepository.count();
    }

    public long countByStatus(Status status) {
        return studentRepository.countByStatus(status);
    }

    private StudentDTO toDTO(Student s) {
        StudentDTO dto = new StudentDTO();
        dto.setId(s.getId());
        dto.setFirstName(s.getFirstName());
        dto.setLastName(s.getLastName());
        dto.setStudentNumber(s.getStudentNumber());
        dto.setDateOfBirth(s.getDateOfBirth());
        dto.setGender(s.getGender());
        dto.setNationality(s.getNationality());
        dto.setNif(s.getNif());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setAddress(s.getAddress());
        dto.setPhoto(s.getPhoto());
        dto.setGuardianName(s.getGuardianName());
        dto.setGuardianContact(s.getGuardianContact());
        dto.setGuardianEmail(s.getGuardianEmail());
        dto.setRelationship(s.getRelationship());
        dto.setPreviousSchool(s.getPreviousSchool());
        dto.setEmergencyContact(s.getEmergencyContact());
        dto.setEmergencyPhone(s.getEmergencyPhone());
        dto.setAllergies(s.getAllergies());
        dto.setMedicalNotes(s.getMedicalNotes());
        dto.setStatus(s.getStatus());
        return dto;
    }

    private Student toEntity(StudentDTO dto) {
        Student s = new Student();
        s.setFirstName(dto.getFirstName());
        s.setLastName(dto.getLastName());
        s.setDateOfBirth(dto.getDateOfBirth());
        s.setGender(dto.getGender());
        s.setNationality(dto.getNationality());
        s.setNif(dto.getNif());
        s.setEmail(dto.getEmail());
        s.setPhone(dto.getPhone());
        s.setAddress(dto.getAddress());
        s.setPhoto(dto.getPhoto());
        s.setGuardianName(dto.getGuardianName());
        s.setGuardianContact(dto.getGuardianContact());
        s.setGuardianEmail(dto.getGuardianEmail());
        s.setRelationship(dto.getRelationship());
        s.setPreviousSchool(dto.getPreviousSchool());
        s.setEmergencyContact(dto.getEmergencyContact());
        s.setEmergencyPhone(dto.getEmergencyPhone());
        s.setAllergies(dto.getAllergies());
        s.setMedicalNotes(dto.getMedicalNotes());
        s.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        return s;
    }
}
