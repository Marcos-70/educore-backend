package com.api.educore.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceCategory category;

    // Class-level pricing: if null, applies to all classes
    @Enumerated(EnumType.STRING)
    private ClassLevel classLevel;

    @Column(nullable = false)
    private double price;

    // Academic year: if null, applies to all years
    private String academicYear;

    // Fine rules - sequential/percentage based
    // 1st-15th: no fine
    // 16th to fineDay2: finePercent1%
    // fineDay2+1 to fineDay3: finePercent2%
    // After fineDay3 or next month: finePercent3%
    private Integer dueDay;              // Day payment is due (default 15)
    private Double finePercent1;         // Fine % after first due date (e.g., 15 = 15%)
    private Integer fineDay2;            // Second threshold day (e.g., 25)
    private Double finePercent2;         // Fine % after second threshold (e.g., 20 = 20%)
    private Integer fineDay3;            // Third threshold day (e.g., end of month)
    private Double finePercent3;         // Fine % if not paid in current month (e.g., 30 = 30%)

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
