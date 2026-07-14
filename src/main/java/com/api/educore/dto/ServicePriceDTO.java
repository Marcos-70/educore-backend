package com.api.educore.dto;

import com.api.educore.model.ServiceCategory;
import lombok.Data;

@Data
public class ServicePriceDTO {
    private Long id;
    private String name;
    private String description;
    private ServiceCategory category;
    private String classLevel;
    private double price;
    private String academicYear;

    // Fine rules - sequential/percentage based
    private Integer dueDay;
    private Double finePercent1;
    private Integer fineDay2;
    private Double finePercent2;
    private Integer fineDay3;
    private Double finePercent3;

    private boolean active;
}
