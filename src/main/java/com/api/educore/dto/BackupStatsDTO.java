package com.api.educore.dto;

import lombok.Data;

@Data
public class BackupStatsDTO {
    private int students;
    private int teachers;
    private int classes;
    private int payments;
    private String lastBackup;
}
