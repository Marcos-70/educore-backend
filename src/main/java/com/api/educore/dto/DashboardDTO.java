package com.api.educore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private long totalStudents;
    private long totalTeachers;
    private long totalClasses;
    private long activeEnrollments;
    private double totalRevenue;
    private double totalPending;
    private long paidPayments;
    private long unpaidPayments;
    private List<RecentActivity> recentActivities;
    private List<TopDebtor> topDebtors;

    @Data
    @AllArgsConstructor
    public static class RecentActivity {
        private String type;
        private String description;
        private String time;
    }

    @Data
    @AllArgsConstructor
    public static class TopDebtor {
        private String studentName;
        private String className;
        private int monthsOverdue;
        private double amount;
    }
}
