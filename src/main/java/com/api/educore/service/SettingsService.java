package com.api.educore.service;

import com.api.educore.dto.SchoolSettingsDTO;
import com.api.educore.model.School;
import com.api.educore.model.SchoolSettings;
import com.api.educore.model.User;
import com.api.educore.repository.SchoolRepository;
import com.api.educore.repository.SchoolSettingsRepository;
import com.api.educore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SchoolSettingsRepository settingsRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    private School getCurrentSchool() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? user.getSchool() : null;
    }

    public SchoolSettingsDTO get() {
        School school = getCurrentSchool();
        SchoolSettings settings = school != null
                ? settingsRepository.findBySchoolId(school.getId()).orElse(new SchoolSettings())
                : settingsRepository.findAll().stream().findFirst().orElse(new SchoolSettings());

        SchoolSettingsDTO dto = toDTO(settings);

        if (school != null) {
            if (dto.getSchoolName() == null || dto.getSchoolName().isEmpty()) dto.setSchoolName(school.getName());
            if (dto.getSchoolMotto() == null || dto.getSchoolMotto().isEmpty()) dto.setSchoolMotto(school.getMotto());
            if (dto.getNif() == null || dto.getNif().isEmpty()) dto.setNif(school.getNif());
            if (dto.getAddress() == null || dto.getAddress().isEmpty()) dto.setAddress(school.getAddress());
            if (dto.getEmail() == null || dto.getEmail().isEmpty()) dto.setEmail(school.getEmail());
            if (dto.getPhone() == null || dto.getPhone().isEmpty()) dto.setPhone(school.getPhone());
            if (dto.getLogoPath() == null || dto.getLogoPath().isEmpty()) dto.setLogoPath(school.getLogo());
        }

        return dto;
    }

    public SchoolSettingsDTO save(SchoolSettingsDTO dto) {
        School school = getCurrentSchool();
        SchoolSettings settings = school != null
                ? settingsRepository.findBySchoolId(school.getId()).orElse(new SchoolSettings())
                : settingsRepository.findAll().stream().findFirst().orElse(new SchoolSettings());
        settings.setSchoolName(dto.getSchoolName());
        settings.setSchoolMotto(dto.getSchoolMotto());
        settings.setNif(dto.getNif());
        settings.setAddress(dto.getAddress());
        settings.setEmail(dto.getEmail());
        settings.setPhone(dto.getPhone());
        settings.setCurrency(dto.getCurrency());
        settings.setTimezone(dto.getTimezone());
        settings.setLogoPath(dto.getLogoPath());
        settings.setActiveAcademicYear(dto.getActiveAcademicYear());
        settings.setAutoBilling(dto.isAutoBilling());
        settings.setAutoBackup(dto.isAutoBackup());
        settings.setEmailNotifications(dto.isEmailNotifications());
        settings.setSmsNotifications(dto.isSmsNotifications());
        settings.setPushNotifications(dto.isPushNotifications());
        settings.setLanguage(dto.getLanguage());
        settings.setDateFormat(dto.getDateFormat());
        settings.setCurrencyFormat(dto.getCurrencyFormat());
        settings.setTheme(dto.getTheme());
        settings.setCompactMode(dto.isCompactMode());
        if (school != null) settings.setSchool(school);
        settingsRepository.save(settings);

        if (school != null) {
            if (dto.getSchoolName() != null && !dto.getSchoolName().isEmpty()) school.setName(dto.getSchoolName());
            if (dto.getSchoolMotto() != null) school.setMotto(dto.getSchoolMotto());
            if (dto.getNif() != null) school.setNif(dto.getNif());
            if (dto.getAddress() != null) school.setAddress(dto.getAddress());
            if (dto.getEmail() != null) school.setEmail(dto.getEmail());
            if (dto.getPhone() != null) school.setPhone(dto.getPhone());
            if (dto.getLogoPath() != null) school.setLogo(dto.getLogoPath());
            schoolRepository.save(school);
        }

        return toDTO(settings);
    }

    private SchoolSettingsDTO toDTO(SchoolSettings s) {
        SchoolSettingsDTO dto = new SchoolSettingsDTO();
        dto.setId(s.getId());
        dto.setSchoolName(s.getSchoolName());
        dto.setSchoolMotto(s.getSchoolMotto());
        dto.setNif(s.getNif());
        dto.setAddress(s.getAddress());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setCurrency(s.getCurrency());
        dto.setTimezone(s.getTimezone());
        dto.setLogoPath(s.getLogoPath());
        dto.setActiveAcademicYear(s.getActiveAcademicYear());
        dto.setAutoBilling(s.isAutoBilling());
        dto.setAutoBackup(s.isAutoBackup());
        dto.setEmailNotifications(s.isEmailNotifications());
        dto.setSmsNotifications(s.isSmsNotifications());
        dto.setPushNotifications(s.isPushNotifications());
        dto.setLanguage(s.getLanguage());
        dto.setDateFormat(s.getDateFormat());
        dto.setCurrencyFormat(s.getCurrencyFormat());
        dto.setTheme(s.getTheme());
        dto.setCompactMode(s.isCompactMode());
        return dto;
    }
}
