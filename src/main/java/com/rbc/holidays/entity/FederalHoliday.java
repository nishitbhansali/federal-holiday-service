package com.rbc.holidays.entity;

import com.rbc.holidays.enums.Country;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "federal_holiday", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"country", "holiday_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FederalHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Holiday name is required")
    @Column(name = "holiday_name", nullable = false, length = 200)
    private String holidayName;

    @NotNull(message = "Holiday date is required")
    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @NotNull(message = "Country is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "country", nullable = false, length = 50)
    private Country country;

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = true;

    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
