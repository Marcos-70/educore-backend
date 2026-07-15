package com.api.educore.service;

import com.api.educore.dto.AttendanceDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SchoolClassRepository classRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public List<AttendanceDTO> findByClassAndDate(Long classId, LocalDate date) {
        School school = getCurrentSchool();
        return attendanceRepository.findBySchoolClassIdAndDate(classId, date)
                .stream()
                .filter(a -> school == null || (a.getSchool() != null && a.getSchool().getId().equals(school.getId())))
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<AttendanceDTO> findByStudent(Long studentId) {
        School school = getCurrentSchool();
        return attendanceRepository.findByStudentId(studentId)
                .stream()
                .filter(a -> school == null || (a.getSchool() != null && a.getSchool().getId().equals(school.getId())))
                .map(this::toDTO).collect(Collectors.toList());
    }

    public void saveAttendance(List<AttendanceDTO> dtos) {
        School school = getCurrentSchool();
        for (AttendanceDTO dto : dtos) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
            SchoolClass sc = classRepository.findById(dto.getSchoolClassId())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

            Attendance attendance = attendanceRepository
                    .findBySchoolClassIdAndDate(dto.getSchoolClassId(), dto.getDate())
                    .stream()
                    .filter(a -> a.getStudent().getId().equals(dto.getStudentId()))
                    .findFirst()
                    .orElse(new Attendance());

            attendance.setStudent(student);
            attendance.setSchoolClass(sc);
            attendance.setDate(dto.getDate());
            attendance.setStatus(dto.getStatus());
            attendance.setReason(dto.getReason());
            attendance.setSchool(school);
            attendanceRepository.save(attendance);
        }
    }

    public long countByClassAndDateAndStatus(Long classId, LocalDate date, AttendanceStatus status) {
        return attendanceRepository.countBySchoolClassIdAndDateAndStatus(classId, date, status);
    }

    private AttendanceDTO toDTO(Attendance a) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(a.getId());
        dto.setStudentId(a.getStudent().getId());
        dto.setStudentName(a.getStudent().getFullName());
        dto.setSchoolClassId(a.getSchoolClass().getId());
        dto.setSchoolClassName(a.getSchoolClass().getName());
        dto.setDate(a.getDate());
        dto.setStatus(a.getStatus());
        dto.setReason(a.getReason());
        return dto;
    }
}
