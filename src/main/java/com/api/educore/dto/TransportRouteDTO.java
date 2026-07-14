package com.api.educore.dto;

import com.api.educore.model.Status;
import lombok.Data;

@Data
public class TransportRouteDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Status status;
    private String origin;
    private String destination;
    private String municipality;
    private String neighborhood;
    private String departureTime;
    private String arrivalTime;
    private Long busId;
    private String busPlateNumber;
    private Long driverId;
    private String driverName;
    private String notes;
}
