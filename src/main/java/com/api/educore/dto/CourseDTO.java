package com.api.educore.dto;

import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    private String code;
    private String name;
    private String abbrevName;
    private String description;
    private String level;
    private String area;
    private String department;
    private int duration;
    private int semesters;
    private int credits;
    private int totalHours;
    private String coordinator;
    private String coordinatorContact;
    private boolean active;
}
