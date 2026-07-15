package com.api.educore.service;

import com.api.educore.dto.AssessmentDTO;
import com.api.educore.dto.GradeDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final TrimesterRepository trimesterRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public List<AssessmentDTO> findAssessments(Long classId, Long subjectId) {
        School school = getCurrentSchool();
        return assessmentRepository.findBySchoolClassIdAndSubjectId(classId, subjectId)
                .stream()
                .filter(a -> school == null || (a.getSchool() != null && a.getSchool().getId().equals(school.getId())))
                .map(this::toAssessmentDTO).collect(Collectors.toList());
    }

    public AssessmentDTO createAssessment(AssessmentDTO dto) {
        Assessment a = new Assessment();
        a.setName(dto.getName());
        a.setType(dto.getType());
        a.setMaxScore(dto.getMaxScore());
        a.setWeight(dto.getWeight());
        a.setDate(dto.getDate());
        SchoolClass sc = classRepository.findById(dto.getSchoolClassId())
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        a.setSchoolClass(sc);
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Disciplina não encontrada"));
        a.setSubject(subject);
        a.setSchool(getCurrentSchool());
        if (dto.getTrimesterId() != null) {
            Trimester t = trimesterRepository.findById(dto.getTrimesterId()).orElse(null);
            a.setTrimester(t);
        }
        return toAssessmentDTO(assessmentRepository.save(a));
    }

    public void deleteAssessment(Long id) {
        assessmentRepository.deleteById(id);
    }

    public List<GradeDTO> findGradesByAssessment(Long assessmentId) {
        School school = getCurrentSchool();
        return gradeRepository.findByAssessmentId(assessmentId)
                .stream()
                .filter(g -> school == null || (g.getSchool() != null && g.getSchool().getId().equals(school.getId())))
                .map(this::toGradeDTO).collect(Collectors.toList());
    }

    public List<GradeDTO> findGradesByStudent(Long studentId) {
        School school = getCurrentSchool();
        return gradeRepository.findByStudentId(studentId)
                .stream()
                .filter(g -> school == null || (g.getSchool() != null && g.getSchool().getId().equals(school.getId())))
                .map(this::toGradeDTO).collect(Collectors.toList());
    }

    public GradeDTO saveGrade(GradeDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        Assessment assessment = assessmentRepository.findById(dto.getAssessmentId())
                .orElseThrow(() -> new RuntimeException("Avaliação não encontrada"));

        Grade grade = gradeRepository.findByStudentIdAndAssessmentId(dto.getStudentId(), dto.getAssessmentId())
                .orElse(new Grade());
        grade.setStudent(student);
        grade.setAssessment(assessment);
        grade.setScore(dto.getScore());
        grade.setObservations(dto.getObservations());
        grade.setSchool(getCurrentSchool());
        return toGradeDTO(gradeRepository.save(grade));
    }

    private AssessmentDTO toAssessmentDTO(Assessment a) {
        AssessmentDTO dto = new AssessmentDTO();
        dto.setId(a.getId());
        dto.setName(a.getName());
        dto.setType(a.getType());
        dto.setSchoolClassId(a.getSchoolClass().getId());
        dto.setSchoolClassName(a.getSchoolClass().getName());
        dto.setSubjectId(a.getSubject().getId());
        dto.setSubjectName(a.getSubject().getName());
        dto.setMaxScore(a.getMaxScore());
        dto.setWeight(a.getWeight());
        dto.setDate(a.getDate());
        dto.setTrimesterId(a.getTrimester() != null ? a.getTrimester().getId() : null);
        return dto;
    }

    private GradeDTO toGradeDTO(Grade g) {
        GradeDTO dto = new GradeDTO();
        dto.setId(g.getId());
        dto.setStudentId(g.getStudent().getId());
        dto.setStudentName(g.getStudent().getFullName());
        dto.setAssessmentId(g.getAssessment().getId());
        dto.setAssessmentName(g.getAssessment().getName());
        dto.setScore(g.getScore());
        dto.setObservations(g.getObservations());
        return dto;
    }
}
