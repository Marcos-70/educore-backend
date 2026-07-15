package com.api.educore.service;

import com.api.educore.dto.TeacherDTO;
import com.api.educore.model.School;
import com.api.educore.model.Status;
import com.api.educore.model.Teacher;
import com.api.educore.model.User;
import com.api.educore.repository.TeacherRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public List<TeacherDTO> findAll() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return teacherRepository.findBySchoolId(school.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TeacherDTO findById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
        return toDTO(teacher);
    }

    public List<TeacherDTO> search(String term) {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return teacherRepository.findByNameContainingIgnoreCase(term)
                .stream()
                .filter(t -> t.getSchool() != null && t.getSchool().getId().equals(school.getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TeacherDTO create(TeacherDTO dto) {
        Teacher teacher = toEntity(dto);
        teacher.setSchool(getCurrentSchool());
        return toDTO(teacherRepository.save(teacher));
    }

    public TeacherDTO update(Long id, TeacherDTO dto) {
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor não encontrado"));
        existing.setName(dto.getName());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());
        existing.setGender(dto.getGender());
        existing.setBirthDate(dto.getBirthDate());
        existing.setBi(dto.getBi());
        existing.setAdmissionDate(dto.getAdmissionDate());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setQualification(dto.getQualification());
        existing.setFormationArea(dto.getFormationArea());
        existing.setDepartment(dto.getDepartment());
        existing.setSubject(dto.getSubject());
        existing.setAddress(dto.getAddress());
        existing.setBio(dto.getBio());
        existing.setPhoto(dto.getPhoto());
        return toDTO(teacherRepository.save(existing));
    }

    public void delete(Long id) {
        teacherRepository.deleteById(id);
    }

    public long count() {
        School school = getCurrentSchool();
        if (school == null) return 0;
        return teacherRepository.findBySchoolId(school.getId()).size();
    }

    public long countByStatus(Status status) {
        School school = getCurrentSchool();
        if (school == null) return 0;
        return teacherRepository.findByStatus(status)
                .stream()
                .filter(t -> t.getSchool() != null && t.getSchool().getId().equals(school.getId()))
                .count();
    }

    private TeacherDTO toDTO(Teacher t) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setEmail(t.getEmail());
        dto.setPhone(t.getPhone());
        dto.setGender(t.getGender());
        dto.setBirthDate(t.getBirthDate());
        dto.setBi(t.getBi());
        dto.setAdmissionDate(t.getAdmissionDate());
        dto.setStatus(t.getStatus());
        dto.setQualification(t.getQualification());
        dto.setFormationArea(t.getFormationArea());
        dto.setDepartment(t.getDepartment());
        dto.setSubject(t.getSubject());
        dto.setAddress(t.getAddress());
        dto.setBio(t.getBio());
        dto.setPhoto(t.getPhoto());
        return dto;
    }

    private Teacher toEntity(TeacherDTO dto) {
        Teacher t = new Teacher();
        t.setName(dto.getName());
        t.setEmail(dto.getEmail());
        t.setPhone(dto.getPhone());
        t.setGender(dto.getGender());
        t.setBirthDate(dto.getBirthDate());
        t.setBi(dto.getBi());
        t.setAdmissionDate(dto.getAdmissionDate());
        t.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        t.setQualification(dto.getQualification());
        t.setFormationArea(dto.getFormationArea());
        t.setDepartment(dto.getDepartment());
        t.setSubject(dto.getSubject());
        t.setAddress(dto.getAddress());
        t.setBio(dto.getBio());
        t.setPhoto(dto.getPhoto());
        return t;
    }
}
