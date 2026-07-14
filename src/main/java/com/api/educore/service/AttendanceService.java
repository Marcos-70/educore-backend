package com.api.educore.service;

import com.api.educore.dto.AttendanceDTO;
import com.api.educore.model.Attendance;
import com.api.educore.model.AttendanceStatus;
import com.api.educore.model.SchoolClass;
import com.api.educore.model.Student;
import com.api.educore.repository.AttendanceRepository;
import com.api.educore.repository.SchoolClassRepository;
import com.api.educore.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
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

    public List<AttendanceDTO> findByClassAndDate(Long classId, LocalDate date) {
        return attendanceRepository.findBySchoolClassIdAndDate(classId, date)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<AttendanceDTO> findByStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void saveAttendance(List<AttendanceDTO> dtos) {
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
