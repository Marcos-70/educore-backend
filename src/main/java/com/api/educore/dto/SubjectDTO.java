package com.api.educore.dto;

import com.api.educore.model.ClassLevel;
import com.api.educore.model.SubjectType;
import lombok.Data;

@Data
public class SubjectDTO {
    private Long id;
    private String code;
    private String name;
    private String abbrevName;
    private String description;
    private ClassLevel classLevel;
    private SubjectType type;
    private double minimumGrade;
    private int minimumAttendance;
    private boolean active;
}
