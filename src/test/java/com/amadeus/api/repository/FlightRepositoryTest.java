package com.amadeus.api.repository;

import com.amadeus.api.entity.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FlightRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FlightRepository flightRepository;

    private Flight activeFlight;
    private Flight inactiveFlight;
    private Flight flightWithNoSeats;
    private LocalDateTime departureTime;
    private LocalDateTime nextDay;

    @BeforeEach
    void setUp() {
        departureTime = LocalDateTime.now().plusDays(1);
        nextDay = departureTime.plusDays(1);

        activeFlight = createFlight("AV123", "BOGOTA", "MADRID", departureTime, true, 100);
        inactiveFlight = createFlight("AV124", "BOGOTA", "MADRID", departureTime.plusHours(2), false, 50);
        flightWithNoSeats = createFlight("AV125", "BOGOTA", "MADRID", departureTime.plusHours(4), true, 0);

        entityManager.persistAndFlush(activeFlight);
        entityManager.persistAndFlush(inactiveFlight);
        entityManager.persistAndFlush(flightWithNoSeats);
    }

    @Test
    void findAvailableFlights_ShouldReturnActiveFlightsWithSeats() {
        List<Flight> flights = flightRepository.findAvailableFlights(
                "BOGOTA", "MADRID", departureTime.minusHours(1), nextDay);

        assertThat(flights).hasSize(1);
        assertThat(flights.get(0).getFlightNumber()).isEqualTo("AV123");
        assertThat(flights.get(0).isActive()).isTrue();
        assertThat(flights.get(0).getAvailableSeats()).isGreaterThan(0);
    }

    @Test
    void findAvailableFlights_ShouldReturnEmptyList_WhenNoMatchingRoute() {
        List<Flight> flights = flightRepository.findAvailableFlights(
                "MADRID", "PARIS", departureTime.minusHours(1), nextDay);

        assertThat(flights).isEmpty();
    }

    @Test
    void findAvailableFlights_ShouldReturnFlightsOrderedByDepartureTime() {
        Flight earlierFlight = createFlight("AV126", "BOGOTA", "MADRID", departureTime.minusHours(2), true, 80);
        Flight laterFlight = createFlight("AV127", "BOGOTA", "MADRID", departureTime.plusHours(1), true, 60);

        entityManager.persistAndFlush(earlierFlight);
        entityManager.persistAndFlush(laterFlight);

        List<Flight> flights = flightRepository.findAvailableFlights(
                "BOGOTA", "MADRID", departureTime.minusHours(3), nextDay);

        assertThat(flights).hasSize(3);
        assertThat(flights.get(0).getFlightNumber()).isEqualTo("AV126");
        assertThat(flights.get(1).getFlightNumber()).isEqualTo("AV123");
        assertThat(flights.get(2).getFlightNumber()).isEqualTo("AV127");
    }

    @Test
    void findAllFlights_ShouldReturnAllFlightsInDateRange_IncludingInactive() {
        List<Flight> flights = flightRepository.findAllFlights(
                "BOGOTA", "MADRID", departureTime.minusHours(1), nextDay);

        assertThat(flights).hasSizeGreaterThanOrEqualTo(2);
        assertThat(flights.stream().anyMatch(f -> f.getFlightNumber().equals("AV123"))).isTrue();
        assertThat(flights.stream().anyMatch(f -> f.getFlightNumber().equals("AV125"))).isTrue();
    }

    @Test
    void findByOriginAndDestinationAndActiveTrue_ShouldReturnActiveFlights() {
        List<Flight> flights = flightRepository.findByOriginAndDestinationAndActiveTrue("BOGOTA", "MADRID");

        assertThat(flights).hasSize(2);
        assertThat(flights.stream().allMatch(Flight::isActive)).isTrue();
        assertThat(flights.stream().anyMatch(f -> f.getFlightNumber().equals("AV123"))).isTrue();
        assertThat(flights.stream().anyMatch(f -> f.getFlightNumber().equals("AV125"))).isTrue();
    }

    @Test
    void existsByFlightNumberAndDepartureTime_ShouldReturnTrue_WhenFlightExists() {
        boolean exists = flightRepository.existsByFlightNumberAndDepartureTime("AV123", departureTime);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByFlightNumberAndDepartureTime_ShouldReturnFalse_WhenFlightDoesNotExist() {
        boolean exists = flightRepository.existsByFlightNumberAndDepartureTime("AV999", departureTime);

        assertThat(exists).isFalse();
    }

    @Test
    void findDistinctOrigins_ShouldReturnUniqueOrigins() {
        Flight parisOriginFlight = createFlight("AV200", "PARIS", "LONDON", departureTime, true, 100);
        entityManager.persistAndFlush(parisOriginFlight);

        List<String> origins = flightRepository.findDistinctOrigins();

        assertThat(origins).containsExactlyInAnyOrder("BOGOTA", "PARIS");
    }

    @Test
    void findDistinctDestinations_ShouldReturnUniqueDestinations() {
        Flight londonDestinationFlight = createFlight("AV201", "PARIS", "LONDON", departureTime, true, 100);
        entityManager.persistAndFlush(londonDestinationFlight);

        List<String> destinations = flightRepository.findDistinctDestinations();

        assertThat(destinations).containsExactlyInAnyOrder("MADRID", "LONDON");
    }

    @Test
    void findDistinctLocations_ShouldReturnUniqueLocationsFromOriginsAndDestinations() {
        Flight parisLondonFlight = createFlight("AV202", "PARIS", "LONDON", departureTime, true, 100);
        entityManager.persistAndFlush(parisLondonFlight);

        List<String> locations = flightRepository.findDistinctLocations();

        assertThat(locations).containsExactlyInAnyOrder("BOGOTA", "MADRID", "PARIS", "LONDON");
    }

    @Test
    void findUpcomingFlights_ShouldReturnFlightsAfterSpecifiedDate() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(2);
        Flight futureFlight = createFlight("AV300", "MADRID", "PARIS", futureTime, true, 150);
        entityManager.persistAndFlush(futureFlight);

        Pageable pageable = PageRequest.of(0, 10);
        List<Flight> upcomingFlights = flightRepository.findUpcomingFlights(departureTime.minusHours(1), pageable);

        assertThat(upcomingFlights).hasSize(3);
        assertThat(upcomingFlights.stream().allMatch(f -> f.getDepartureTime().isAfter(departureTime.minusHours(1))))
                .isTrue();
        assertThat(upcomingFlights.stream().allMatch(Flight::isActive)).isTrue();
    }

    @Test
    void findUpcomingFlights_ShouldRespectPageableLimit() {
        for (int i = 0; i < 5; i++) {
            Flight flight = createFlight("AV40" + i, "MADRID", "PARIS", departureTime.plusHours(i), true, 100);
            entityManager.persistAndFlush(flight);
        }

        Pageable pageable = PageRequest.of(0, 3);
        List<Flight> upcomingFlights = flightRepository.findUpcomingFlights(departureTime.minusHours(1), pageable);

        assertThat(upcomingFlights).hasSize(3);
    }

    @Test
    void searchFlightsByMultipleFields_ShouldFindByFlightNumber() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("AV123", pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getFlightNumber()).isEqualTo("AV123");
    }

    @Test
    void searchFlightsByMultipleFields_ShouldFindByAirline() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("Avianca", pageable);

        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getContent().stream().allMatch(f -> f.getAirline().contains("Avianca"))).isTrue();
    }

    @Test
    void searchFlightsByMultipleFields_ShouldFindByOrigin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("BOGOTA", pageable);

        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getContent().stream().allMatch(f -> f.getOrigin().equals("BOGOTA"))).isTrue();
    }

    @Test
    void searchFlightsByMultipleFields_ShouldFindByDestination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("MADRID", pageable);

        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getContent().stream().allMatch(f -> f.getDestination().equals("MADRID"))).isTrue();
    }

    @Test
    void searchFlightsByMultipleFields_ShouldFindByAircraftType() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("Boeing", pageable);

        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getContent().stream().allMatch(f -> f.getAircraftType().contains("Boeing"))).isTrue();
    }

    @Test
    void searchFlightsByMultipleFields_ShouldFindByCabinClass() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("Economy", pageable);

        assertThat(results.getContent()).hasSize(3);
        assertThat(results.getContent().stream().allMatch(f -> f.getCabinClass().equals("Economy"))).isTrue();
    }

    @Test
    void searchFlightsByMultipleFields_ShouldBeCaseInsensitive() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("av123", pageable);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getFlightNumber()).isEqualTo("AV123");
    }

    @Test
    void searchFlightsByMultipleFields_ShouldReturnEmptyPage_WhenNoMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("NONEXISTENT", pageable);

        assertThat(results.getContent()).isEmpty();
        assertThat(results.getTotalElements()).isZero();
    }

    @Test
    void searchFlightsByMultipleFields_ShouldOrderByDepartureTimeDesc() {
        Flight earlierFlight = createFlight("AV500", "BOGOTA", "MADRID", departureTime.minusHours(2), true, 100);
        Flight laterFlight = createFlight("AV501", "BOGOTA", "MADRID", departureTime.plusHours(2), true, 100);

        entityManager.persistAndFlush(earlierFlight);
        entityManager.persistAndFlush(laterFlight);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Flight> results = flightRepository.searchFlightsByMultipleFields("BOGOTA", pageable);

        List<Flight> flights = results.getContent();
        assertThat(flights).hasSizeGreaterThanOrEqualTo(2);

        for (int i = 0; i < flights.size() - 1; i++) {
            assertThat(flights.get(i).getDepartureTime())
                    .isAfterOrEqualTo(flights.get(i + 1).getDepartureTime());
        }
    }

    @Test
    void findDistinctOrigins_ShouldReturnOnlyActiveFlights() {
        Flight inactiveOriginFlight = createFlight("AV600", "INACTIVE_ORIGIN", "MADRID", departureTime, false, 100);
        entityManager.persistAndFlush(inactiveOriginFlight);

        List<String> origins = flightRepository.findDistinctOrigins();

        assertThat(origins).doesNotContain("INACTIVE_ORIGIN");
        assertThat(origins).contains("BOGOTA");
    }

    @Test
    void findDistinctDestinations_ShouldReturnOnlyActiveFlights() {
        Flight inactiveDestinationFlight = createFlight("AV601", "BOGOTA", "INACTIVE_DEST", departureTime, false, 100);
        entityManager.persistAndFlush(inactiveDestinationFlight);

        List<String> destinations = flightRepository.findDistinctDestinations();

        assertThat(destinations).doesNotContain("INACTIVE_DEST");
        assertThat(destinations).contains("MADRID");
    }

    private Flight createFlight(String flightNumber, String origin, String destination,
            LocalDateTime departureTime, boolean active, int availableSeats) {
        return Flight.builder()
                .flightNumber(flightNumber)
                .airline("Avianca")
                .origin(origin)
                .destination(destination)
                .departureTime(departureTime)
                .arrivalTime(departureTime.plusHours(8))
                .duration("8h 00m")
                .price(new BigDecimal("500000"))
                .aircraftType("Boeing 787")
                .availableSeats(availableSeats)
                .cabinClass("Economy")
                .active(active)
                .build();
    }
}
