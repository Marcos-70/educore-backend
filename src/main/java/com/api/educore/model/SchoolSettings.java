package com.api.educore.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "school_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String schoolName;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
