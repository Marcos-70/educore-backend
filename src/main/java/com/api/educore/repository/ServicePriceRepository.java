package com.api.educore.repository;

import com.api.educore.model.ClassLevel;
import com.api.educore.model.ServiceCategory;
import com.api.educore.model.ServicePrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicePriceRepository extends JpaRepository<ServicePrice, Long> {
    List<ServicePrice> findByActiveTrue();
    List<ServicePrice> findByCategory(ServiceCategory category);
    Optional<ServicePrice> findFirstByCategoryAndActiveTrueOrderByPriceDesc(ServiceCategory category);
    Optional<ServicePrice> findFirstByCategoryAndClassLevelAndActiveTrueOrderByPriceDesc(ServiceCategory category, ClassLevel classLevel);
    Optional<ServicePrice> findFirstByCategoryAndClassLevelIsNullAndActiveTrueOrderByPriceDesc(ServiceCategory category);
}
