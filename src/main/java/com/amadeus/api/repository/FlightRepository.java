package com.amadeus.api.repository;

import com.amadeus.api.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f WHERE f.origin = :origin " +
            "AND f.destination = :destination " +
            "AND DATE(f.departureTime) = :departureDate " +
            "AND f.active = true " +
            "AND f.availableSeats > 0 " +
            "ORDER BY f.departureTime")
    List<Flight> findAvailableFlights(@Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureDate") LocalDate departureDate);

    @Query("SELECT f FROM Flight f WHERE f.origin = :origin " +
            "AND f.destination = :destination " +
            "AND DATE(f.departureTime) = :departureDate " +
            "AND f.active = true " +
            "ORDER BY f.departureTime")
    List<Flight> findAllFlights(@Param("origin") String origin,
            @Param("destination") String destination,
            @Param("departureDate") LocalDate departureDate);

    List<Flight> findByOriginAndDestinationAndActiveTrue(String origin, String destination);

    boolean existsByFlightNumberAndDepartureTime(String flightNumber, LocalDateTime departureTime);
}
