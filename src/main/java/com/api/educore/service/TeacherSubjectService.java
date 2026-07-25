package com.api.educore.service;

import com.api.educore.dto.TeacherSubjectDTO;
import com.api.educore.model.Subject;
import com.api.educore.model.Teacher;
import com.api.educore.model.TeacherSubject;
import com.api.educore.repository.SubjectRepository;
import com.api.educore.repository.TeacherRepository;
import com.api.educore.repository.TeacherSubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherSubjectService {

    private final TeacherSubjectRepository teacherSubjectRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<TeacherSubjectDTO> getByTeacher(Long teacherId) {
        return teacherSubjectRepository.findByTeacherIdWithSubject(teacherId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeacherSubjectDTO> getBySubject(Long subjectId) {
        return teacherSubjectRepository.findBySubjectIdWithTeacher(subjectId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public TeacherSubjectDTO add(Long teacherId, Long subjectId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Professor nao encontrado"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Disciplina nao encontrada"));

        if (teacherSubjectRepository.existsByTeacherIdAndSubjectId(teacherId, subjectId)) {
            throw new RuntimeException("Esta disciplina ja esta associada ao professor");
        }

        TeacherSubject ts = TeacherSubject.builder()
                .teacher(teacher)
                .subject(subject)
                .school(teacher.getSchool())
                .build();
        return toDTO(teacherSubjectRepository.save(ts));
    }

    @Transactional
    public void remove(Long teacherId, Long subjectId) {
        teacherSubjectRepository.deleteByTeacherIdAndSubjectId(teacherId, subjectId);
    }

    private TeacherSubjectDTO toDTO(TeacherSubject ts) {
        return TeacherSubjectDTO.builder()
                .id(ts.getId())
                .teacherId(ts.getTeacher().getId())
                .teacherName(ts.getTeacher().getName())
                .subjectId(ts.getSubject().getId())
                .subjectName(ts.getSubject().getName())
                .subjectCode(ts.getSubject().getCode())
                .build();
    }
}
