package com.api.educore.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ScheduleDTO {
    private Long id;
    private Long schoolClassId;
    private String schoolClassName;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private String dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
}
