package com.api.educore.repository;

import com.api.educore.model.SubjectGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectGradeRepository extends JpaRepository<SubjectGrade, Long> {

    @Query("SELECT sg FROM SubjectGrade sg " +
           "LEFT JOIN FETCH sg.student " +
           "LEFT JOIN FETCH sg.subject " +
           "LEFT JOIN FETCH sg.trimester " +
           "LEFT JOIN FETCH sg.schoolClass " +
           "LEFT JOIN FETCH sg.school " +
           "WHERE sg.schoolClass.id = :classId AND sg.subject.id = :subjectId AND sg.trimester.id = :trimesterId")
    List<SubjectGrade> findBySchoolClassIdAndSubjectIdAndTrimesterId(
            @Param("classId") Long schoolClassId, @Param("subjectId") Long subjectId, @Param("trimesterId") Long trimesterId);

    List<SubjectGrade> findBySchoolClassIdAndTrimesterId(Long schoolClassId, Long trimesterId);

    @Query("SELECT sg FROM SubjectGrade sg " +
           "LEFT JOIN FETCH sg.student " +
           "LEFT JOIN FETCH sg.subject " +
           "WHERE sg.student.id = :studentId AND sg.subject.id = :subjectId AND sg.trimester.id = :trimesterId")
    Optional<SubjectGrade> findByStudentIdAndSubjectIdAndTrimesterId(
            @Param("studentId") Long studentId, @Param("subjectId") Long subjectId, @Param("trimesterId") Long trimesterId);

    List<SubjectGrade> findByStudentIdAndTrimesterId(Long studentId, Long trimesterId);

    @Query("SELECT sg FROM SubjectGrade sg " +
           "LEFT JOIN FETCH sg.student " +
           "LEFT JOIN FETCH sg.subject " +
           "LEFT JOIN FETCH sg.trimester " +
           "LEFT JOIN FETCH sg.schoolClass " +
           "LEFT JOIN FETCH sg.school " +
           "WHERE sg.schoolClass.id = :classId AND sg.trimester.id = :trimesterId " +
           "ORDER BY sg.student.firstName, sg.student.lastName")
    List<SubjectGrade> findByClassAndTrimesterOrdered(@Param("classId") Long classId, @Param("trimesterId") Long trimesterId);

    boolean existsByStudentIdAndSubjectIdAndTrimesterId(Long studentId, Long subjectId, Long trimesterId);

    void deleteBySchoolClassIdAndSubjectIdAndTrimesterId(Long schoolClassId, Long subjectId, Long trimesterId);
}
