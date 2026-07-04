package com.rbc.holidays.repository;

import com.rbc.holidays.entity.FederalHoliday;
import com.rbc.holidays.enums.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for FederalHoliday entity.
 * 
 * <p>This repository provides database access methods for managing federal holidays.
 * Spring Data JPA auto-generates implementation for method name-based queries.
 * Custom JPQL queries are defined using @Query annotation.</p>
 * 
 * <p><strong>Query Strategies:</strong></p>
 * <ul>
 *   <li>Method name queries - Spring auto-generates SQL (findByCountry, existsByCountryAndHolidayDate)</li>
 *   <li>@Query JPQL - Custom queries for complex logic (findByYear, findByCountryAndYear)</li>
 * </ul>
 */
@Repository
public interface HolidayRepository extends JpaRepository<FederalHoliday, Long> {

    /**
     * Finds all holidays for a specific country.
     * 
     * @param country Country to filter by (USA or CANADA)
     * @return List of holidays for the country
     */
    List<FederalHoliday> findByCountry(Country country);

    /**
     * Finds a specific holiday by country and date.
     * 
     * @param country Country of the holiday
     * @param holidayDate Date of the holiday
     * @return Optional containing the holiday if found
     */
    Optional<FederalHoliday> findByCountryAndHolidayDate(Country country, LocalDate holidayDate);

    /**
     * Checks if a holiday exists for a specific country and date.
     * Used for duplicate validation before creating/updating holidays.
     * 
     * @param country Country to check
     * @param holidayDate Date to check
     * @return true if holiday exists, false otherwise
     */
    boolean existsByCountryAndHolidayDate(Country country, LocalDate holidayDate);

    /**
     * Finds all holidays within a date range.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of holidays in the date range
     */
    List<FederalHoliday> findByHolidayDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Finds holidays for a specific country within a date range.
     * Custom JPQL query for combined filtering.
     * 
     * @param country Country to filter by
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of holidays matching criteria
     */
    @Query("SELECT h FROM FederalHoliday h WHERE h.country = :country AND h.holidayDate BETWEEN :startDate AND :endDate")
    List<FederalHoliday> findByCountryAndDateRange(
            @Param("country") Country country,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Finds all holidays in a specific year.
     * Uses JPQL YEAR function to extract year from date.
     * 
     * @param year Year to filter by (e.g., 2026)
     * @return List of holidays in that year
     */
    @Query("SELECT h FROM FederalHoliday h WHERE YEAR(h.holidayDate) = :year")
    List<FederalHoliday> findByYear(@Param("year") int year);

    /**
     * Finds holidays for a specific country and year.
     * Combines country filtering with YEAR function.
     * 
     * @param country Country to filter by
     * @param year Year to filter by
     * @return List of holidays matching criteria
     */
    @Query("SELECT h FROM FederalHoliday h WHERE h.country = :country AND YEAR(h.holidayDate) = :year")
    List<FederalHoliday> findByCountryAndYear(
            @Param("country") Country country,
            @Param("year") int year);
}
