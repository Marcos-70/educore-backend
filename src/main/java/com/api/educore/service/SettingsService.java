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
        if (school == null) {
            return toDTO(new SchoolSettings());
        }

        SchoolSettings settings = settingsRepository.findBySchoolId(school.getId())
                .orElseGet(() -> {
                    SchoolSettings newSettings = new SchoolSettings();
                    newSettings.setSchool(school);
                    return settingsRepository.save(newSettings);
                });

        SchoolSettingsDTO dto = toDTO(settings);

        if (dto.getSchoolName() == null || dto.getSchoolName().isEmpty()) dto.setSchoolName(school.getName());
        if (dto.getSchoolMotto() == null) dto.setSchoolMotto(school.getMotto());
        if (dto.getNif() == null || dto.getNif().isEmpty()) dto.setNif(school.getNif());
        if (dto.getAddress() == null || dto.getAddress().isEmpty()) dto.setAddress(school.getAddress());
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) dto.setEmail(school.getEmail());
        if (dto.getPhone() == null || dto.getPhone().isEmpty()) dto.setPhone(school.getPhone());
        if (dto.getLogoPath() == null || dto.getLogoPath().isEmpty()) dto.setLogoPath(school.getLogo());

        return dto;
    }

    public SchoolSettingsDTO save(SchoolSettingsDTO dto) {
        School school = getCurrentSchool();
        if (school == null) {
            throw new RuntimeException("Escola não encontrada para o usuário atual");
        }

        SchoolSettings settings = settingsRepository.findBySchoolId(school.getId())
                .orElseGet(() -> {
                    SchoolSettings newSettings = new SchoolSettings();
                    newSettings.setSchool(school);
                    return newSettings;
                });

        if (dto.getSchoolName() != null) settings.setSchoolName(dto.getSchoolName());
        if (dto.getSchoolMotto() != null) settings.setSchoolMotto(dto.getSchoolMotto());
        if (dto.getNif() != null) settings.setNif(dto.getNif());
        if (dto.getAddress() != null) settings.setAddress(dto.getAddress());
        if (dto.getEmail() != null) settings.setEmail(dto.getEmail());
        if (dto.getPhone() != null) settings.setPhone(dto.getPhone());
        if (dto.getCurrency() != null) settings.setCurrency(dto.getCurrency());
        if (dto.getTimezone() != null) settings.setTimezone(dto.getTimezone());
        if (dto.getLogoPath() != null) settings.setLogoPath(dto.getLogoPath());
        if (dto.getActiveAcademicYear() != null) settings.setActiveAcademicYear(dto.getActiveAcademicYear());
        settings.setAutoBilling(dto.isAutoBilling());
        settings.setAutoBackup(dto.isAutoBackup());
        settings.setEmailNotifications(dto.isEmailNotifications());
        settings.setSmsNotifications(dto.isSmsNotifications());
        settings.setPushNotifications(dto.isPushNotifications());
        if (dto.getLanguage() != null) settings.setLanguage(dto.getLanguage());
        if (dto.getDateFormat() != null) settings.setDateFormat(dto.getDateFormat());
        if (dto.getCurrencyFormat() != null) settings.setCurrencyFormat(dto.getCurrencyFormat());
        if (dto.getTheme() != null) settings.setTheme(dto.getTheme());
        settings.setCompactMode(dto.isCompactMode());

        settingsRepository.save(settings);

        if (dto.getSchoolName() != null && !dto.getSchoolName().isEmpty()) school.setName(dto.getSchoolName());
        if (dto.getSchoolMotto() != null) school.setMotto(dto.getSchoolMotto());
        if (dto.getNif() != null && !dto.getNif().isEmpty()) school.setNif(dto.getNif());
        if (dto.getAddress() != null && !dto.getAddress().isEmpty()) school.setAddress(dto.getAddress());
        if (dto.getEmail() != null && !dto.getEmail().isEmpty()) school.setEmail(dto.getEmail());
        if (dto.getPhone() != null && !dto.getPhone().isEmpty()) school.setPhone(dto.getPhone());
        if (dto.getLogoPath() != null && !dto.getLogoPath().isEmpty()) school.setLogo(dto.getLogoPath());
        schoolRepository.save(school);

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
