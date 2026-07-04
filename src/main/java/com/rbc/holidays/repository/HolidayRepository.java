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

@Repository
public interface HolidayRepository extends JpaRepository<FederalHoliday, Long> {

    List<FederalHoliday> findByCountry(Country country);

    Optional<FederalHoliday> findByCountryAndHolidayDate(Country country, LocalDate holidayDate);

    boolean existsByCountryAndHolidayDate(Country country, LocalDate holidayDate);

    List<FederalHoliday> findByHolidayDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT h FROM FederalHoliday h WHERE h.country = :country AND h.holidayDate BETWEEN :startDate AND :endDate")
    List<FederalHoliday> findByCountryAndDateRange(
            @Param("country") Country country,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM FederalHoliday h WHERE YEAR(h.holidayDate) = :year")
    List<FederalHoliday> findByYear(@Param("year") int year);

    @Query("SELECT h FROM FederalHoliday h WHERE h.country = :country AND YEAR(h.holidayDate) = :year")
    List<FederalHoliday> findByCountryAndYear(
            @Param("country") Country country,
            @Param("year") int year);
}
