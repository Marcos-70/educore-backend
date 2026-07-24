package com.api.educore.service;

import com.api.educore.dto.AssessmentDTO;
import com.api.educore.dto.GradeDTO;
import com.api.educore.dto.ReportCardDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private final EnrollmentRepository enrollmentRepository;

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
        return gradeRepository.findByAssessmentOrderedByStudent(assessmentId)
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

    // Boletim de notas de um aluno
    public ReportCardDTO getReportCard(Long studentId, Long classId, Long trimesterId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        Trimester trimester = trimesterRepository.findById(trimesterId)
                .orElseThrow(() -> new RuntimeException("Trimestre não encontrado"));

        // Buscar todas as notas do aluno na turma e trimestre
        List<Grade> grades = gradeRepository.findByStudentAndClassAndTrimester(studentId, classId, trimesterId);

        // Agrupar por disciplina
        Map<Long, List<Grade>> gradesBySubject = new LinkedHashMap<>();
        for (Grade g : grades) {
            Long subjectId = g.getAssessment().getSubject().getId();
            gradesBySubject.computeIfAbsent(subjectId, k -> new ArrayList<>()).add(g);
        }

        // Construir notas por disciplina
        List<ReportCardDTO.SubjectGrades> subjects = new ArrayList<>();
        double totalAverage = 0;
        int subjectCount = 0;

        for (Map.Entry<Long, List<Grade>> entry : gradesBySubject.entrySet()) {
            List<Grade> subjectGrades = entry.getValue();
            Subject subject = subjectGrades.get(0).getAssessment().getSubject();

            List<ReportCardDTO.GradeEntry> gradeEntries = new ArrayList<>();
            double weightedSum = 0;
            double totalWeight = 0;

            for (Grade g : subjectGrades) {
                Assessment a = g.getAssessment();
                gradeEntries.add(ReportCardDTO.GradeEntry.builder()
                        .assessmentId(a.getId())
                        .assessmentName(a.getName())
                        .assessmentType(a.getType())
                        .score(g.getScore())
                        .maxScore(a.getMaxScore())
                        .weight(a.getWeight())
                        .build());

                // Media ponderada da disciplina
                double normalizedScore = (g.getScore() / a.getMaxScore()) * 20; // Normalizar para 20
                weightedSum += normalizedScore * a.getWeight();
                totalWeight += a.getWeight();
            }

            double subjectAvg = totalWeight > 0 ? weightedSum / totalWeight : 0;

            subjects.add(ReportCardDTO.SubjectGrades.builder()
                    .subjectId(subject.getId())
                    .subjectName(subject.getName())
                    .grades(gradeEntries)
                    .subjectAverage(Math.round(subjectAvg * 100.0) / 100.0)
                    .assessmentCount(gradeEntries.size())
                    .build());

            totalAverage += subjectAvg;
            subjectCount++;
        }

        // Media trimestral
        double overallAvg = subjectCount > 0 ? totalAverage / subjectCount : 0;
        overallAvg = Math.round(overallAvg * 100.0) / 100.0;

        String classification = getClassification(overallAvg);
        boolean passed = overallAvg >= 10; // 10/20 = 50%

        return ReportCardDTO.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .className(schoolClass.getName())
                .academicYear(trimester.getAcademicYear().getName())
                .trimesterName(trimester.getName())
                .subjects(subjects)
                .overallAverage(overallAvg)
                .classification(classification)
                .passed(passed)
                .build();
    }

    // Boletins de todos os alunos de uma turma
    public List<ReportCardDTO> getReportCardsByClass(Long classId, Long trimesterId) {
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));

        // Buscar todos os alunos da turma via matriculas
        List<Enrollment> enrollments = enrollmentRepository.findBySchoolClassId(classId);
        List<Student> students = enrollments.stream()
                .map(Enrollment::getStudent)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return students.stream()
                .map(s -> {
                    try {
                        return getReportCard(s.getId(), classId, trimesterId);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String getClassification(double average) {
        if (average >= 18) return "Excelente";
        if (average >= 16) return "Muito Bom";
        if (average >= 14) return "Bom";
        if (average >= 12) return "Suficiente";
        if (average >= 10) return "Regular";
        if (average >= 8) return "Insuficiente";
        if (average >= 6) return "Mau";
        return "Muito Mau";
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
