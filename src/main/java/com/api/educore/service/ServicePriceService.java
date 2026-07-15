package com.api.educore.service;

import com.api.educore.dto.ServicePriceDTO;
import com.api.educore.model.ClassLevel;
import com.api.educore.model.School;
import com.api.educore.model.ServiceCategory;
import com.api.educore.model.ServicePrice;
import com.api.educore.model.User;
import com.api.educore.repository.ServicePriceRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicePriceService {

    private final ServicePriceRepository repository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public List<ServicePriceDTO> findAll() {
        School school = getCurrentSchool();
        if (school == null) return List.of();
        return repository.findBySchoolId(school.getId()).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ServicePriceDTO> findActive() {
        return repository.findByActiveTrue().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ServicePriceDTO findByCategory(String categoryName) {
        ServiceCategory category = ServiceCategory.valueOf(categoryName.toUpperCase());
        ServicePrice price = repository.findFirstByCategoryAndActiveTrueOrderByPriceDesc(category)
                .orElseThrow(() -> new RuntimeException("Preco nao encontrado para a categoria: " + categoryName));
        return toDTO(price);
    }

    public List<ServicePriceDTO> listByCategory(String categoryName) {
        ServiceCategory category = ServiceCategory.valueOf(categoryName.toUpperCase());
        return repository.findByCategory(category).stream()
                .filter(ServicePrice::isActive)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ServicePriceDTO findByCategoryAndClass(String categoryName, String classLevelName) {
        ServiceCategory category = ServiceCategory.valueOf(categoryName.toUpperCase());
        ClassLevel classLevel = ClassLevel.valueOf(classLevelName.toUpperCase());

        ServicePrice price = repository.findFirstByCategoryAndClassLevelAndActiveTrueOrderByPriceDesc(category, classLevel)
                .orElseGet(() -> repository.findFirstByCategoryAndClassLevelIsNullAndActiveTrueOrderByPriceDesc(category)
                        .orElseThrow(() -> new RuntimeException("Preco nao encontrado para: " + categoryName + " - " + classLevelName)));
        return toDTO(price);
    }

    public ServicePriceDTO create(ServicePriceDTO dto) {
        ServicePrice entity = new ServicePrice();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setClassLevel(parseClassLevel(dto.getClassLevel()));
        entity.setPrice(dto.getPrice());
        entity.setAcademicYear(dto.getAcademicYear());
        entity.setDueDay(dto.getDueDay());
        entity.setFinePercent1(dto.getFinePercent1());
        entity.setFineDay2(dto.getFineDay2());
        entity.setFinePercent2(dto.getFinePercent2());
        entity.setFineDay3(dto.getFineDay3());
        entity.setFinePercent3(dto.getFinePercent3());
        entity.setActive(dto.isActive());
        entity.setSchool(getCurrentSchool());
        return toDTO(repository.save(entity));
    }

    public ServicePriceDTO update(Long id, ServicePriceDTO dto) {
        ServicePrice existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servico nao encontrado"));
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setCategory(dto.getCategory());
        existing.setClassLevel(parseClassLevel(dto.getClassLevel()));
        existing.setPrice(dto.getPrice());
        existing.setAcademicYear(dto.getAcademicYear());
        existing.setDueDay(dto.getDueDay());
        existing.setFinePercent1(dto.getFinePercent1());
        existing.setFineDay2(dto.getFineDay2());
        existing.setFinePercent2(dto.getFinePercent2());
        existing.setFineDay3(dto.getFineDay3());
        existing.setFinePercent3(dto.getFinePercent3());
        existing.setActive(dto.isActive());
        return toDTO(repository.save(existing));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private ClassLevel parseClassLevel(String classLevelName) {
        if (classLevelName == null || classLevelName.isBlank()) return null;
        try {
            return ClassLevel.valueOf(classLevelName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ServicePriceDTO toDTO(ServicePrice e) {
        ServicePriceDTO dto = new ServicePriceDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setCategory(e.getCategory());
        dto.setClassLevel(e.getClassLevel() != null ? e.getClassLevel().name() : null);
        dto.setPrice(e.getPrice());
        dto.setAcademicYear(e.getAcademicYear());
        dto.setDueDay(e.getDueDay());
        dto.setFinePercent1(e.getFinePercent1());
        dto.setFineDay2(e.getFineDay2());
        dto.setFinePercent2(e.getFinePercent2());
        dto.setFineDay3(e.getFineDay3());
        dto.setFinePercent3(e.getFinePercent3());
        dto.setActive(e.isActive());
        return dto;
    }
}
