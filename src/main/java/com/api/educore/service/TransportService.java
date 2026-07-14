package com.api.educore.service;

import com.api.educore.dto.BusDTO;
import com.api.educore.dto.DriverDTO;
import com.api.educore.dto.TransportRouteDTO;
import com.api.educore.model.*;
import com.api.educore.repository.BusRepository;
import com.api.educore.repository.DriverRepository;
import com.api.educore.repository.TransportRouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRouteRepository routeRepository;
    private final BusRepository busRepository;
    private final DriverRepository driverRepository;

    // Routes
    public List<TransportRouteDTO> findAllRoutes() {
        return routeRepository.findAll().stream().map(this::toRouteDTO).collect(Collectors.toList());
    }

    public TransportRouteDTO createRoute(TransportRouteDTO dto) {
        TransportRoute route = new TransportRoute();
        mapRoute(dto, route);
        return toRouteDTO(routeRepository.save(route));
    }

    public TransportRouteDTO updateRoute(Long id, TransportRouteDTO dto) {
        TransportRoute existing = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rota não encontrada"));
        mapRoute(dto, existing);
        return toRouteDTO(routeRepository.save(existing));
    }

    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }

    // Buses
    public List<BusDTO> findAllBuses() {
        return busRepository.findAll().stream().map(this::toBusDTO).collect(Collectors.toList());
    }

    public BusDTO createBus(BusDTO dto) {
        Bus bus = new Bus();
        mapBus(dto, bus);
        return toBusDTO(busRepository.save(bus));
    }

    public BusDTO updateBus(Long id, BusDTO dto) {
        Bus existing = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Autocarro não encontrado"));
        mapBus(dto, existing);
        return toBusDTO(busRepository.save(existing));
    }

    public void deleteBus(Long id) {
        busRepository.deleteById(id);
    }

    // Drivers
    public List<DriverDTO> findAllDrivers() {
        return driverRepository.findAll().stream().map(this::toDriverDTO).collect(Collectors.toList());
    }

    public DriverDTO createDriver(DriverDTO dto) {
        Driver driver = new Driver();
        mapDriver(dto, driver);
        return toDriverDTO(driverRepository.save(driver));
    }

    public DriverDTO updateDriver(Long id, DriverDTO dto) {
        Driver existing = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Condutor não encontrado"));
        mapDriver(dto, existing);
        return toDriverDTO(driverRepository.save(existing));
    }

    public void deleteDriver(Long id) {
        driverRepository.deleteById(id);
    }

    // Mapping
    private void mapRoute(TransportRouteDTO dto, TransportRoute r) {
        r.setCode(dto.getCode());
        r.setName(dto.getName());
        r.setDescription(dto.getDescription());
        r.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        r.setOrigin(dto.getOrigin());
        r.setDestination(dto.getDestination());
        r.setMunicipality(dto.getMunicipality());
        r.setNeighborhood(dto.getNeighborhood());
        r.setDepartureTime(dto.getDepartureTime());
        r.setArrivalTime(dto.getArrivalTime());
        r.setNotes(dto.getNotes());
        if (dto.getBusId() != null) {
            Bus bus = busRepository.findById(dto.getBusId()).orElse(null);
            r.setBus(bus);
        }
        if (dto.getDriverId() != null) {
            Driver driver = driverRepository.findById(dto.getDriverId()).orElse(null);
            r.setDriver(driver);
        }
    }

    private void mapBus(BusDTO dto, Bus b) {
        b.setCode(dto.getCode());
        b.setPlateNumber(dto.getPlateNumber());
        b.setBrand(dto.getBrand());
        b.setModel(dto.getModel());
        b.setYear(dto.getYear());
        b.setCapacity(dto.getCapacity());
        b.setSeats(dto.getSeats());
        b.setFuelType(dto.getFuelType());
        b.setColor(dto.getColor());
        b.setInsuranceNumber(dto.getInsuranceNumber());
        b.setInsuranceExpiry(dto.getInsuranceExpiry());
        b.setNextInspection(dto.getNextInspection());
        b.setMileage(dto.getMileage());
        b.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        b.setAcquisitionDate(dto.getAcquisitionDate());
        b.setNotes(dto.getNotes());
    }

    private void mapDriver(DriverDTO dto, Driver d) {
        d.setCode(dto.getCode());
        d.setName(dto.getName());
        d.setGender(dto.getGender());
        d.setBirthDate(dto.getBirthDate());
        d.setDocumentId(dto.getDocumentId());
        d.setNif(dto.getNif());
        d.setPhone(dto.getPhone());
        d.setPhone2(dto.getPhone2());
        d.setEmail(dto.getEmail());
        d.setAddress(dto.getAddress());
        d.setLicenseNumber(dto.getLicenseNumber());
        d.setLicenseCategory(dto.getLicenseCategory());
        d.setLicenseIssueDate(dto.getLicenseIssueDate());
        d.setLicenseExpiry(dto.getLicenseExpiry());
        d.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.ACTIVE);
        d.setAdmissionDate(dto.getAdmissionDate());
        d.setNotes(dto.getNotes());
        d.setAdminNotes(dto.getAdminNotes());
        if (dto.getAssignedBusId() != null) {
            Bus bus = busRepository.findById(dto.getAssignedBusId()).orElse(null);
            d.setAssignedBus(bus);
        }
        if (dto.getAssignedRouteId() != null) {
            TransportRoute route = routeRepository.findById(dto.getAssignedRouteId()).orElse(null);
            d.setAssignedRoute(route);
        }
    }

    private TransportRouteDTO toRouteDTO(TransportRoute r) {
        TransportRouteDTO dto = new TransportRouteDTO();
        dto.setId(r.getId());
        dto.setCode(r.getCode());
        dto.setName(r.getName());
        dto.setDescription(r.getDescription());
        dto.setStatus(r.getStatus());
        dto.setOrigin(r.getOrigin());
        dto.setDestination(r.getDestination());
        dto.setMunicipality(r.getMunicipality());
        dto.setNeighborhood(r.getNeighborhood());
        dto.setDepartureTime(r.getDepartureTime());
        dto.setArrivalTime(r.getArrivalTime());
        dto.setBusId(r.getBus() != null ? r.getBus().getId() : null);
        dto.setBusPlateNumber(r.getBus() != null ? r.getBus().getPlateNumber() : null);
        dto.setDriverId(r.getDriver() != null ? r.getDriver().getId() : null);
        dto.setDriverName(r.getDriver() != null ? r.getDriver().getName() : null);
        dto.setNotes(r.getNotes());
        return dto;
    }

    private BusDTO toBusDTO(Bus b) {
        BusDTO dto = new BusDTO();
        dto.setId(b.getId());
        dto.setCode(b.getCode());
        dto.setPlateNumber(b.getPlateNumber());
        dto.setBrand(b.getBrand());
        dto.setModel(b.getModel());
        dto.setYear(b.getYear());
        dto.setCapacity(b.getCapacity());
        dto.setSeats(b.getSeats());
        dto.setFuelType(b.getFuelType());
        dto.setColor(b.getColor());
        dto.setInsuranceNumber(b.getInsuranceNumber());
        dto.setInsuranceExpiry(b.getInsuranceExpiry());
        dto.setNextInspection(b.getNextInspection());
        dto.setMileage(b.getMileage());
        dto.setStatus(b.getStatus());
        dto.setAcquisitionDate(b.getAcquisitionDate());
        dto.setNotes(b.getNotes());
        return dto;
    }

    private DriverDTO toDriverDTO(Driver d) {
        DriverDTO dto = new DriverDTO();
        dto.setId(d.getId());
        dto.setCode(d.getCode());
        dto.setName(d.getName());
        dto.setGender(d.getGender());
        dto.setBirthDate(d.getBirthDate());
        dto.setDocumentId(d.getDocumentId());
        dto.setNif(d.getNif());
        dto.setPhone(d.getPhone());
        dto.setPhone2(d.getPhone2());
        dto.setEmail(d.getEmail());
        dto.setAddress(d.getAddress());
        dto.setLicenseNumber(d.getLicenseNumber());
        dto.setLicenseCategory(d.getLicenseCategory());
        dto.setLicenseIssueDate(d.getLicenseIssueDate());
        dto.setLicenseExpiry(d.getLicenseExpiry());
        dto.setStatus(d.getStatus());
        dto.setAdmissionDate(d.getAdmissionDate());
        dto.setAssignedBusId(d.getAssignedBus() != null ? d.getAssignedBus().getId() : null);
        dto.setAssignedBusPlate(d.getAssignedBus() != null ? d.getAssignedBus().getPlateNumber() : null);
        dto.setAssignedRouteId(d.getAssignedRoute() != null ? d.getAssignedRoute().getId() : null);
        dto.setAssignedRouteName(d.getAssignedRoute() != null ? d.getAssignedRoute().getName() : null);
        dto.setNotes(d.getNotes());
        dto.setAdminNotes(d.getAdminNotes());
        return dto;
    }
}
