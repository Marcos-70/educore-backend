package com.api.educore.service;

import com.api.educore.dto.SchoolSettingsDTO;
import com.api.educore.model.SchoolSettings;
import com.api.educore.repository.SchoolSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SchoolSettingsRepository settingsRepository;

    public SchoolSettingsDTO get() {
        SchoolSettings settings = settingsRepository.findAll().stream().findFirst()
                .orElse(new SchoolSettings());
        return toDTO(settings);
    }

    public SchoolSettingsDTO save(SchoolSettingsDTO dto) {
        SchoolSettings settings = settingsRepository.findAll().stream().findFirst()
                .orElse(new SchoolSettings());
        settings.setSchoolName(dto.getSchoolName());
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
        return toDTO(settingsRepository.save(settings));
    }

    private SchoolSettingsDTO toDTO(SchoolSettings s) {
        SchoolSettingsDTO dto = new SchoolSettingsDTO();
        dto.setId(s.getId());
        dto.setSchoolName(s.getSchoolName());
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
