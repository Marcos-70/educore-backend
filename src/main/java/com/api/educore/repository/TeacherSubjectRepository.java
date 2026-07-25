package com.api.educore.repository;

import com.api.educore.model.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {

    List<TeacherSubject> findByTeacherId(Long teacherId);

    List<TeacherSubject> findBySubjectId(Long subjectId);

    boolean existsByTeacherIdAndSubjectId(Long teacherId, Long subjectId);

    void deleteByTeacherIdAndSubjectId(Long teacherId, Long subjectId);

    @Query("SELECT ts FROM TeacherSubject ts JOIN FETCH ts.subject WHERE ts.teacher.id = :teacherId")
    List<TeacherSubject> findByTeacherIdWithSubject(@Param("teacherId") Long teacherId);

    @Query("SELECT ts FROM TeacherSubject ts JOIN FETCH ts.teacher WHERE ts.subject.id = :subjectId")
    List<TeacherSubject> findBySubjectIdWithTeacher(@Param("subjectId") Long subjectId);
}
