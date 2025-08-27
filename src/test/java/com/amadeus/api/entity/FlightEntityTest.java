package com.amadeus.api.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FlightEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void onCreate_ShouldSetCreatedAtAndUpdatedAt() {
        Flight flight = createTestFlight();

        LocalDateTime beforePersist = LocalDateTime.now().minusSeconds(5);

        entityManager.persistAndFlush(flight);

        LocalDateTime afterPersist = LocalDateTime.now().plusSeconds(5);

        assertThat(flight.getCreatedAt()).isNotNull();
        assertThat(flight.getUpdatedAt()).isNotNull();
        assertThat(flight.getCreatedAt()).isAfter(beforePersist);
        assertThat(flight.getCreatedAt()).isBefore(afterPersist);
        assertThat(flight.getUpdatedAt()).isAfter(beforePersist);
        assertThat(flight.getUpdatedAt()).isBefore(afterPersist);
        assertThat(flight.getCreatedAt().toLocalDate()).isEqualTo(flight.getUpdatedAt().toLocalDate());
    }

    @Test
    void onUpdate_ShouldUpdateOnlyUpdatedAt() throws InterruptedException {
        Flight flight = createTestFlight();

        entityManager.persistAndFlush(flight);

        LocalDateTime originalCreatedAt = flight.getCreatedAt();
        LocalDateTime originalUpdatedAt = flight.getUpdatedAt();

        Thread.sleep(100);

        flight.setAirline("Updated Airline");

        LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);
        entityManager.persistAndFlush(flight);
        LocalDateTime afterUpdate = LocalDateTime.now().plusSeconds(1);

        assertThat(flight.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(flight.getUpdatedAt()).isNotEqualTo(originalUpdatedAt);
        assertThat(flight.getUpdatedAt()).isAfter(beforeUpdate);
        assertThat(flight.getUpdatedAt()).isBefore(afterUpdate);
    }

    @Test
    void flightBuilder_ShouldCreateValidFlight() {
        LocalDateTime departureTime = LocalDateTime.now().plusDays(1);
        LocalDateTime arrivalTime = departureTime.plusHours(8);
        BigDecimal price = new BigDecimal("500000.00");

        Flight flight = Flight.builder()
                .flightNumber("AV123")
                .airline("Avianca")
                .origin("BOGOTA")
                .destination("MADRID")
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .duration("8h 00m")
                .price(price)
                .aircraftType("Boeing 787")
                .availableSeats(200)
                .cabinClass("Economy")
                .active(true)
                .build();

        assertThat(flight.getFlightNumber()).isEqualTo("AV123");
        assertThat(flight.getAirline()).isEqualTo("Avianca");
        assertThat(flight.getOrigin()).isEqualTo("BOGOTA");
        assertThat(flight.getDestination()).isEqualTo("MADRID");
        assertThat(flight.getDepartureTime()).isEqualTo(departureTime);
        assertThat(flight.getArrivalTime()).isEqualTo(arrivalTime);
        assertThat(flight.getDuration()).isEqualTo("8h 00m");
        assertThat(flight.getPrice()).isEqualByComparingTo(price);
        assertThat(flight.getAircraftType()).isEqualTo("Boeing 787");
        assertThat(flight.getAvailableSeats()).isEqualTo(200);
        assertThat(flight.getCabinClass()).isEqualTo("Economy");
        assertThat(flight.isActive()).isTrue();
    }

    @Test
    void flightDefaultValues_ShouldBeCorrect() {
        Flight flight = new Flight();

        assertThat(flight.getId()).isNull();
        assertThat(flight.getCreatedAt()).isNull();
        assertThat(flight.getUpdatedAt()).isNull();
        assertThat(flight.isActive()).isFalse();
    }

    @Test
    void flightSettersAndGetters_ShouldWorkCorrectly() {
        Flight flight = new Flight();

        Long id = 1L;
        String flightNumber = "AV456";
        String airline = "Latam";
        String origin = "CALI";
        String destination = "PARIS";
        LocalDateTime departureTime = LocalDateTime.now().plusDays(2);
        LocalDateTime arrivalTime = departureTime.plusHours(10);
        String duration = "10h 30m";
        BigDecimal price = new BigDecimal("750000.00");
        String aircraftType = "Airbus A350";
        Integer availableSeats = 180;
        String cabinClass = "Business";
        boolean active = true;
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now().minusHours(2);

        flight.setId(id);
        flight.setFlightNumber(flightNumber);
        flight.setAirline(airline);
        flight.setOrigin(origin);
        flight.setDestination(destination);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setDuration(duration);
        flight.setPrice(price);
        flight.setAircraftType(aircraftType);
        flight.setAvailableSeats(availableSeats);
        flight.setCabinClass(cabinClass);
        flight.setActive(active);
        flight.setCreatedAt(createdAt);
        flight.setUpdatedAt(updatedAt);

        assertThat(flight.getId()).isEqualTo(id);
        assertThat(flight.getFlightNumber()).isEqualTo(flightNumber);
        assertThat(flight.getAirline()).isEqualTo(airline);
        assertThat(flight.getOrigin()).isEqualTo(origin);
        assertThat(flight.getDestination()).isEqualTo(destination);
        assertThat(flight.getDepartureTime()).isEqualTo(departureTime);
        assertThat(flight.getArrivalTime()).isEqualTo(arrivalTime);
        assertThat(flight.getDuration()).isEqualTo(duration);
        assertThat(flight.getPrice()).isEqualByComparingTo(price);
        assertThat(flight.getAircraftType()).isEqualTo(aircraftType);
        assertThat(flight.getAvailableSeats()).isEqualTo(availableSeats);
        assertThat(flight.getCabinClass()).isEqualTo(cabinClass);
        assertThat(flight.isActive()).isEqualTo(active);
        assertThat(flight.getCreatedAt()).isEqualTo(createdAt);
        assertThat(flight.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void flightEquals_ShouldWorkCorrectly() {
        Flight flight1 = Flight.builder()
                .id(1L)
                .flightNumber("AV123")
                .airline("Avianca")
                .build();

        Flight flight2 = Flight.builder()
                .id(1L)
                .flightNumber("AV123")
                .airline("Avianca")
                .build();

        Flight flight3 = Flight.builder()
                .id(2L)
                .flightNumber("AV456")
                .airline("Latam")
                .build();

        assertThat(flight1).isEqualTo(flight2);
        assertThat(flight1).isNotEqualTo(flight3);
        assertThat(flight1.hashCode()).isEqualTo(flight2.hashCode());
        assertThat(flight1.hashCode()).isNotEqualTo(flight3.hashCode());
    }

    @Test
    void flightToString_ShouldContainMainFields() {
        Flight flight = Flight.builder()
                .flightNumber("AV123")
                .airline("Avianca")
                .origin("BOGOTA")
                .destination("MADRID")
                .build();

        String toString = flight.toString();

        assertThat(toString).contains("AV123");
        assertThat(toString).contains("Avianca");
        assertThat(toString).contains("BOGOTA");
        assertThat(toString).contains("MADRID");
    }

    @Test
    void flightPersistence_ShouldMaintainDataIntegrity() {
        Flight flight = createTestFlight();

        entityManager.persistAndFlush(flight);
        entityManager.clear();

        Flight retrievedFlight = entityManager.find(Flight.class, flight.getId());

        assertThat(retrievedFlight).isNotNull();
        assertThat(retrievedFlight.getFlightNumber()).isEqualTo(flight.getFlightNumber());
        assertThat(retrievedFlight.getAirline()).isEqualTo(flight.getAirline());
        assertThat(retrievedFlight.getOrigin()).isEqualTo(flight.getOrigin());
        assertThat(retrievedFlight.getDestination()).isEqualTo(flight.getDestination());
        assertThat(retrievedFlight.getPrice()).isEqualByComparingTo(flight.getPrice());
        assertThat(retrievedFlight.isActive()).isEqualTo(flight.isActive());
    }

    @Test
    void multipleUpdates_ShouldOnlyChangeUpdatedAt() throws InterruptedException {
        Flight flight = createTestFlight();

        entityManager.persistAndFlush(flight);

        LocalDateTime originalCreatedAt = flight.getCreatedAt();
        LocalDateTime originalUpdatedAt = flight.getUpdatedAt();

        Thread.sleep(50);

        flight.setAirline("First Update");
        entityManager.persistAndFlush(flight);

        LocalDateTime firstUpdateTime = flight.getUpdatedAt();

        Thread.sleep(50);

        flight.setAirline("Second Update");
        entityManager.persistAndFlush(flight);

        LocalDateTime secondUpdateTime = flight.getUpdatedAt();

        assertThat(flight.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(firstUpdateTime).isAfter(originalUpdatedAt);
        assertThat(secondUpdateTime).isAfter(firstUpdateTime);
    }

    private Flight createTestFlight() {
        return Flight.builder()
                .flightNumber("AV123")
                .airline("Avianca")
                .origin("BOGOTA")
                .destination("MADRID")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(8))
                .duration("8h 00m")
                .price(new BigDecimal("500000"))
                .aircraftType("Boeing 787")
                .availableSeats(200)
                .cabinClass("Economy")
                .active(true)
                .build();
    }
}
