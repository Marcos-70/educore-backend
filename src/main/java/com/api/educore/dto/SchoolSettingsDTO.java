package com.api.educore.dto;

import lombok.Data;

@Data
public class SchoolSettingsDTO {
    private Long id;
    private String schoolName;
    private String schoolMotto;
    private String nif;
    private String address;
    private String email;
    private String phone;
    private String currency;
    private String timezone;
    private String logoPath;
    private String activeAcademicYear;
    private boolean autoBilling;
    private boolean autoBackup;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean pushNotifications;
    private String language;
    private String dateFormat;
    private String currencyFormat;
    private String theme;
    private boolean compactMode;
}
