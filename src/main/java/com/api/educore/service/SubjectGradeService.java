package com.api.educore.service;

import com.api.educore.dto.ReportCardDTO;
import com.api.educore.dto.SubjectGradeDTO;
import com.api.educore.model.*;
import com.api.educore.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectGradeService {

    private final SubjectGradeRepository subjectGradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final TrimesterRepository trimesterRepository;
    private final SchoolClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;

    private School getCurrentSchool() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email).orElse(null);
            return user != null ? user.getSchool() : null;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<SubjectGradeDTO> listGrades(Long classId, Long subjectId, Long trimesterId) {
        School school = getCurrentSchool();

        List<Enrollment> enrollments = enrollmentRepository.findBySchoolClassIdWithStudent(classId);
        List<Student> students = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.APPROVED)
                .map(Enrollment::getStudent)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a))
                .values()
                .stream()
                .toList();

        List<SubjectGrade> existing = subjectGradeRepository
                .findBySchoolClassIdAndSubjectIdAndTrimesterId(classId, subjectId, trimesterId);

        Map<Long, SubjectGrade> directGradeMap = existing.stream()
                .collect(Collectors.toMap(
                        g -> g.getStudent().getId(),
                        g -> g,
                        (a, b) -> a
                ));

        List<Assessment> assessments = assessmentRepository
                .findBySchoolClassIdAndSubjectIdAndTrimesterId(classId, subjectId, trimesterId);

        Map<Long, List<Grade>> assessmentGradesByStudent = new HashMap<>();
        if (!assessments.isEmpty()) {
            List<Grade> allAssessmentGrades = new ArrayList<>();
            for (Assessment a : assessments) {
                List<Grade> gradesForAssessment = gradeRepository.findByAssessmentOrderedByStudent(a.getId());
                allAssessmentGrades.addAll(gradesForAssessment);
            }
            assessmentGradesByStudent = allAssessmentGrades.stream()
                    .collect(Collectors.groupingBy(g -> g.getStudent().getId()));
        }

        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        Trimester trimester = trimesterRepository.findById(trimesterId).orElse(null);

        List<SubjectGradeDTO> result = new ArrayList<>();
        for (Student s : students) {
            SubjectGrade sg = directGradeMap.get(s.getId());
            SubjectGradeDTO dto = new SubjectGradeDTO();
            dto.setStudentId(s.getId());
            dto.setStudentName(s.getFullName());
            dto.setSubjectId(subjectId);
            dto.setSubjectName(subject != null ? subject.getName() : null);
            dto.setTrimesterId(trimesterId);
            dto.setTrimesterName(trimester != null ? trimester.getName() : null);
            dto.setClassId(classId);

            if (sg != null) {
                dto.setId(sg.getId());
                dto.setScore(sg.getScore());
                dto.setObservations(sg.getObservations());
            } else {
                List<Grade> studentAssessmentGrades = assessmentGradesByStudent.getOrDefault(s.getId(), List.of());
                if (!studentAssessmentGrades.isEmpty()) {
                    double weightedSum = 0;
                    double totalWeight = 0;
                    for (Grade g : studentAssessmentGrades) {
                        Assessment a = g.getAssessment();
                        double maxScore = a.getMaxScore();
                        double score = g.getScore();
                        if (maxScore > 0 && !Double.isNaN(score)) {
                            double normalizedScore = (score / maxScore) * 20;
                            double weight = a.getWeight();
                            weightedSum += normalizedScore * weight;
                            totalWeight += weight;
                        }
                    }
                    double avg = totalWeight > 0 ? weightedSum / totalWeight : 0;
                    avg = Double.isNaN(avg) ? 0 : avg;
                    dto.setScore(Math.round(avg * 100.0) / 100.0);
                } else {
                    dto.setScore(0);
                }
            }
            result.add(dto);
        }
        return result;
    }

    public List<SubjectGradeDTO> saveBatch(List<SubjectGradeDTO> dtos) {
        School school = getCurrentSchool();
        List<SubjectGradeDTO> saved = new ArrayList<>();

        for (SubjectGradeDTO dto : dtos) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Aluno não encontrado: " + dto.getStudentId()));
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Disciplina não encontrada: " + dto.getSubjectId()));
            Trimester trimester = trimesterRepository.findById(dto.getTrimesterId())
                    .orElseThrow(() -> new RuntimeException("Trimestre não encontrado: " + dto.getTrimesterId()));
            SchoolClass schoolClass = classRepository.findById(dto.getClassId())
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada: " + dto.getClassId()));

            if (dto.getScore() < 0 || dto.getScore() > 20) {
                throw new RuntimeException("A nota deve estar entre 0 e 20");
            }

            Optional<SubjectGrade> existing = subjectGradeRepository
                    .findByStudentIdAndSubjectIdAndTrimesterId(
                            dto.getStudentId(), dto.getSubjectId(), dto.getTrimesterId());

            if (existing.isPresent()) {
                if (dto.getId() == null) {
                    throw new RuntimeException(
                            "Já existe uma nota para o aluno " + student.getFullName() +
                            " na disciplina " + subject.getName() +
                            " no trimestre " + trimester.getName() +
                            ". Não é permitido registar mais de uma nota por disciplina por trimestre.");
                }
                SubjectGrade sg = existing.get();
                sg.setScore(dto.getScore());
                sg.setObservations(dto.getObservations());
                sg.setSchool(school);
                saved.add(toDTO(subjectGradeRepository.save(sg)));
            } else {
                SubjectGrade sg = new SubjectGrade();
                sg.setStudent(student);
                sg.setSubject(subject);
                sg.setTrimester(trimester);
                sg.setSchoolClass(schoolClass);
                sg.setScore(dto.getScore());
                sg.setObservations(dto.getObservations());
                sg.setSchool(school);
                saved.add(toDTO(subjectGradeRepository.save(sg)));
            }
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<ReportCardDTO> getReportCards(Long classId, Long trimesterId) {
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        Trimester trimester = trimesterRepository.findByIdWithAcademicYear(trimesterId)
                .orElseThrow(() -> new RuntimeException("Trimestre não encontrado"));

        List<Enrollment> enrollments = enrollmentRepository.findBySchoolClassIdWithStudent(classId);
        List<Student> students = enrollments.stream()
                .map(Enrollment::getStudent)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a))
                .values()
                .stream()
                .toList();

        List<Assessment> allAssessments = assessmentRepository.findBySchoolClassIdWithTrimesterAndSubject(classId);
        List<Assessment> trimesterAssessments = allAssessments.stream()
                .filter(a -> a.getTrimester() != null && a.getTrimester().getId().equals(trimesterId))
                .toList();

        Map<Long, List<Assessment>> assessmentsBySubject = trimesterAssessments.stream()
                .collect(Collectors.groupingBy(a -> a.getSubject().getId(), LinkedHashMap::new, Collectors.toList()));

        Map<Long, Map<Long, List<Grade>>> gradesByStudentAndSubject = new HashMap<>();
        for (Assessment a : trimesterAssessments) {
            List<Grade> grades = gradeRepository.findByAssessmentOrderedByStudent(a.getId());
            for (Grade g : grades) {
                gradesByStudentAndSubject
                        .computeIfAbsent(g.getStudent().getId(), k -> new LinkedHashMap<>())
                        .computeIfAbsent(a.getSubject().getId(), k -> new ArrayList<>())
                        .add(g);
            }
        }

        List<SubjectGrade> directGrades = subjectGradeRepository.findByClassAndTrimesterOrdered(classId, trimesterId);
        Map<Long, Map<Long, SubjectGrade>> directGradeMap = new HashMap<>();
        for (SubjectGrade sg : directGrades) {
            directGradeMap
                    .computeIfAbsent(sg.getStudent().getId(), k -> new HashMap<>())
                    .put(sg.getSubject().getId(), sg);
        }

        List<ReportCardDTO> reportCards = new ArrayList<>();

        for (Student student : students) {
            Map<Long, SubjectGrade> studentDirectGrades = directGradeMap.getOrDefault(student.getId(), Map.of());
            Map<Long, List<Grade>> studentAssessmentGrades = gradesByStudentAndSubject.getOrDefault(student.getId(), Map.of());

            Set<Long> allSubjectIds = new LinkedHashSet<>();
            allSubjectIds.addAll(studentDirectGrades.keySet());
            allSubjectIds.addAll(studentAssessmentGrades.keySet());
            for (Map.Entry<Long, List<Assessment>> e : assessmentsBySubject.entrySet()) {
                allSubjectIds.add(e.getKey());
            }

            List<ReportCardDTO.SubjectGrades> subjects = new ArrayList<>();
            double totalAverage = 0;
            int subjectCount = 0;

            for (Long subId : allSubjectIds) {
                Subject subject = subjectRepository.findById(subId).orElse(null);
                if (subject == null) continue;

                SubjectGrade directGrade = studentDirectGrades.get(subId);
                List<Grade> assessmentGrades = studentAssessmentGrades.getOrDefault(subId, List.of());

                List<ReportCardDTO.GradeEntry> gradeEntries = new ArrayList<>();
                double subjectAvg;

                if (directGrade != null) {
                    gradeEntries.add(ReportCardDTO.GradeEntry.builder()
                            .assessmentId(directGrade.getId())
                            .assessmentName(subject.getName())
                            .assessmentType("subject_grade")
                            .score(directGrade.getScore())
                            .maxScore(20.0)
                            .weight(1.0)
                            .build());
                    subjectAvg = directGrade.getScore();
                } else if (!assessmentGrades.isEmpty()) {
                    double weightedSum = 0;
                    double totalWeight = 0;
                    for (Grade g : assessmentGrades) {
                        Assessment a = g.getAssessment();
                        double maxScore = a.getMaxScore();
                        double score = g.getScore();
                        gradeEntries.add(ReportCardDTO.GradeEntry.builder()
                                .assessmentId(a.getId())
                                .assessmentName(a.getName())
                                .assessmentType(a.getType())
                                .score(score)
                                .maxScore(maxScore)
                                .weight(a.getWeight())
                                .build());
                        if (maxScore > 0 && !Double.isNaN(score)) {
                            double normalizedScore = (score / maxScore) * 20;
                            weightedSum += normalizedScore * a.getWeight();
                            totalWeight += a.getWeight();
                        }
                    }
                    subjectAvg = totalWeight > 0 ? weightedSum / totalWeight : 0;
                } else {
                    subjectAvg = 0;
                }

                subjectAvg = Math.round(subjectAvg * 100.0) / 100.0;
                subjectAvg = Double.isNaN(subjectAvg) ? 0 : subjectAvg;

                subjects.add(ReportCardDTO.SubjectGrades.builder()
                        .subjectId(subId)
                        .subjectName(subject.getName())
                        .grades(gradeEntries)
                        .subjectAverage(subjectAvg)
                        .assessmentCount(gradeEntries.size())
                        .build());

                totalAverage += subjectAvg;
                subjectCount++;
            }

            double overallAvg = subjectCount > 0 ? totalAverage / subjectCount : 0;
            overallAvg = Math.round(overallAvg * 100.0) / 100.0;
            overallAvg = Double.isNaN(overallAvg) ? 0 : overallAvg;
            String classification = getClassification(overallAvg);
            boolean passed = overallAvg >= 10;

            reportCards.add(ReportCardDTO.builder()
                    .studentId(student.getId())
                    .studentName(student.getFullName())
                    .className(schoolClass.getName())
                    .academicYear(trimester.getAcademicYear().getName())
                    .trimesterName(trimester.getName())
                    .subjects(subjects)
                    .overallAverage(overallAvg)
                    .classification(classification)
                    .passed(passed)
                    .build());
        }

        return reportCards;
    }

    @Transactional(readOnly = true)
    public ReportCardDTO getStudentReportCard(Long studentId, Long classId, Long trimesterId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Aluno não encontrado"));
        SchoolClass schoolClass = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
        Trimester trimester = trimesterRepository.findByIdWithAcademicYear(trimesterId)
                .orElseThrow(() -> new RuntimeException("Trimestre não encontrado"));

        List<SubjectGrade> directGrades = subjectGradeRepository
                .findByStudentIdAndTrimesterId(studentId, trimesterId);
        Map<Long, SubjectGrade> directGradeMap = directGrades.stream()
                .filter(sg -> sg.getSchoolClass().getId().equals(classId))
                .collect(Collectors.toMap(sg -> sg.getSubject().getId(), sg -> sg, (a, b) -> a));

        List<Grade> assessmentGrades = gradeRepository
                .findByStudentAndClassAndTrimester(studentId, classId, trimesterId);
        Map<Long, List<Grade>> assessmentGradesBySubject = assessmentGrades.stream()
                .collect(Collectors.groupingBy(g -> g.getAssessment().getSubject().getId()));

        Set<Long> allSubjectIds = new LinkedHashSet<>();
        allSubjectIds.addAll(directGradeMap.keySet());
        allSubjectIds.addAll(assessmentGradesBySubject.keySet());

        List<ReportCardDTO.SubjectGrades> subjects = new ArrayList<>();
        double totalAverage = 0;
        int subjectCount = 0;

        for (Long subId : allSubjectIds) {
            Subject subject = subjectRepository.findById(subId).orElse(null);
            if (subject == null) continue;

            SubjectGrade directGrade = directGradeMap.get(subId);
            List<Grade> studentAssessmentGrades = assessmentGradesBySubject.getOrDefault(subId, List.of());

            List<ReportCardDTO.GradeEntry> gradeEntries = new ArrayList<>();
            double subjectAvg;

            if (directGrade != null) {
                gradeEntries.add(ReportCardDTO.GradeEntry.builder()
                        .assessmentId(directGrade.getId())
                        .assessmentName(subject.getName())
                        .assessmentType("subject_grade")
                        .score(directGrade.getScore())
                        .maxScore(20.0)
                        .weight(1.0)
                        .build());
                subjectAvg = directGrade.getScore();
            } else if (!studentAssessmentGrades.isEmpty()) {
                double weightedSum = 0;
                double totalWeight = 0;
                for (Grade g : studentAssessmentGrades) {
                    Assessment a = g.getAssessment();
                    double maxScore = a.getMaxScore();
                    double score = g.getScore();
                    gradeEntries.add(ReportCardDTO.GradeEntry.builder()
                            .assessmentId(a.getId())
                            .assessmentName(a.getName())
                            .assessmentType(a.getType())
                            .score(score)
                            .maxScore(maxScore)
                            .weight(a.getWeight())
                            .build());
                    if (maxScore > 0 && !Double.isNaN(score)) {
                        double normalizedScore = (score / maxScore) * 20;
                        weightedSum += normalizedScore * a.getWeight();
                        totalWeight += a.getWeight();
                    }
                }
                subjectAvg = totalWeight > 0 ? weightedSum / totalWeight : 0;
            } else {
                subjectAvg = 0;
            }

            subjectAvg = Math.round(subjectAvg * 100.0) / 100.0;
            subjectAvg = Double.isNaN(subjectAvg) ? 0 : subjectAvg;

            subjects.add(ReportCardDTO.SubjectGrades.builder()
                    .subjectId(subId)
                    .subjectName(subject.getName())
                    .grades(gradeEntries)
                    .subjectAverage(subjectAvg)
                    .assessmentCount(gradeEntries.size())
                    .build());

            totalAverage += subjectAvg;
            subjectCount++;
        }

        double overallAvg = subjectCount > 0 ? totalAverage / subjectCount : 0;
        overallAvg = Math.round(overallAvg * 100.0) / 100.0;
        overallAvg = Double.isNaN(overallAvg) ? 0 : overallAvg;
        String classification = getClassification(overallAvg);
        boolean passed = overallAvg >= 10;

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

    private SubjectGradeDTO toDTO(SubjectGrade sg) {
        SubjectGradeDTO dto = new SubjectGradeDTO();
        dto.setId(sg.getId());
        dto.setStudentId(sg.getStudent().getId());
        dto.setStudentName(sg.getStudent().getFullName());
        dto.setSubjectId(sg.getSubject().getId());
        dto.setSubjectName(sg.getSubject().getName());
        dto.setTrimesterId(sg.getTrimester().getId());
        dto.setTrimesterName(sg.getTrimester().getName());
        dto.setClassId(sg.getSchoolClass().getId());
        dto.setScore(sg.getScore());
        dto.setObservations(sg.getObservations());
        return dto;
    }
}
