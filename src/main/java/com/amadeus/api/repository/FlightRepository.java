package com.amadeus.api.repository;

import com.amadeus.api.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

        @Query("SELECT f FROM Flight f WHERE f.origin = :origin " +
                        "AND f.destination = :destination " +
                        "AND f.departureTime >= :departureDate " +
                        "AND f.departureTime < :nextDay " +
                        "AND f.active = true " +
                        "AND f.availableSeats > 0 " +
                        "ORDER BY f.departureTime")
        List<Flight> findAvailableFlights(@Param("origin") String origin,
                        @Param("destination") String destination,
                        @Param("departureDate") LocalDateTime departureDate,
                        @Param("nextDay") LocalDateTime nextDay);

        @Query("SELECT f FROM Flight f WHERE f.origin = :origin " +
                        "AND f.destination = :destination " +
                        "AND f.departureTime >= :departureDate " +
                        "AND f.departureTime < :nextDay " +
                        "AND f.active = true " +
                        "ORDER BY f.departureTime")
        List<Flight> findAllFlights(@Param("origin") String origin,
                        @Param("destination") String destination,
                        @Param("departureDate") LocalDateTime departureDate,
                        @Param("nextDay") LocalDateTime nextDay);

        List<Flight> findByOriginAndDestinationAndActiveTrue(String origin, String destination);

        boolean existsByFlightNumberAndDepartureTime(String flightNumber, LocalDateTime departureTime);

        @Query("SELECT DISTINCT f.origin FROM Flight f WHERE f.active = true ORDER BY f.origin")
        List<String> findDistinctOrigins();

        @Query("SELECT DISTINCT f.destination FROM Flight f WHERE f.active = true ORDER BY f.destination")
        List<String> findDistinctDestinations();

        @Query("SELECT DISTINCT f.origin FROM Flight f WHERE f.active = true " +
                        "UNION " +
                        "SELECT DISTINCT f.destination FROM Flight f WHERE f.active = true " +
                        "ORDER BY 1")
        List<String> findDistinctLocations();
}
