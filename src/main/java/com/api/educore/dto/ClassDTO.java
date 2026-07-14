package com.api.educore.dto;

import com.api.educore.model.ClassLevel;
import com.api.educore.model.Shift;
import lombok.Data;

@Data
public class ClassDTO {
    private Long id;
    private String name;
    private ClassLevel classLevel;
    private String course;
    private Long teacherId;
    private String teacherName;
    private String room;
    private int capacity;
    private Shift shift;
    private String academicYear;
    private int enrolledCount;
}
