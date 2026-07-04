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

/**
 * JPA Entity representing a Federal Holiday.
 * 
 * <p>This entity stores federal holidays for different countries (USA, CANADA)
 * with support for both one-time and recurring holidays.</p>
 * 
 * <p><strong>Database Constraints:</strong></p>
 * <ul>
 *   <li>Unique constraint on (country, holiday_date) - prevents duplicate holidays</li>
 *   <li>Auto-generated ID using IDENTITY strategy</li>
 *   <li>Audit timestamps (createdAt, updatedAt) managed by JPA</li>
 * </ul>
 * 
 * <p><strong>Design Patterns:</strong></p>
 * <ul>
 *   <li>Uses Lombok for reducing boilerplate (getters, setters, builder)</li>
 *   <li>Hibernate annotations for automatic timestamp management</li>
 *   <li>Bean validation annotations for data integrity</li>
 * </ul>
 */
@Entity
@Table(name = "federal_holiday", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"country", "holiday_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FederalHoliday {

    /**
     * Unique identifier for the holiday.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the federal holiday.
     * Examples: "Independence Day", "Canada Day", "Thanksgiving"
     */
    @NotBlank(message = "Holiday name is required")
    @Column(name = "holiday_name", nullable = false, length = 200)
    private String holidayName;

    /**
     * Date when the holiday occurs.
     * For recurring holidays, this represents the date in a specific year.
     */
    @NotNull(message = "Holiday date is required")
    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    /**
     * Country for which this holiday applies (USA or CANADA).
     * Stored as STRING in database for readability.
     */
    @NotNull(message = "Country is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "country", nullable = false, length = 50)
    private Country country;

    /**
     * Indicates if the holiday recurs annually.
     * Defaults to true (most federal holidays recur yearly).
     */
    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = true;

    /**
     * Optional description or significance of the holiday.
     * Example: "Celebrates the independence of the United States"
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Timestamp when the record was created.
     * Automatically set by JPA on insert.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the record was last updated.
     * Automatically updated by JPA on every update.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
