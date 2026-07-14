package com.api.educore.repository;

import com.api.educore.model.Status;
import com.api.educore.model.TransportRoute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportRouteRepository extends JpaRepository<TransportRoute, Long> {
    List<TransportRoute> findByStatus(Status status);
}
