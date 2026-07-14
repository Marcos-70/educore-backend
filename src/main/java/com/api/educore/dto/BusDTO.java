package com.api.educore.dto;

import com.api.educore.model.Status;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BusDTO {
    private Long id;
    private String code;
    private String plateNumber;
    private String brand;
    private String model;
    private int year;
    private int capacity;
    private int seats;
    private String fuelType;
    private String color;
    private String insuranceNumber;
    private LocalDate insuranceExpiry;
    private LocalDate nextInspection;
    private int mileage;
    private Status status;
    private LocalDate acquisitionDate;
    private String notes;
}
