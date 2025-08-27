package com.amadeus.api.dto.validation;

import com.amadeus.api.dto.request.FlightSearchRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FlightSearchRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validFlightSearchRequest_ShouldPassValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(2)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void validFlightSearchRequestRoundtrip_ShouldPassValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .returnDate(LocalDate.now().plusDays(7))
                .tripType("roundtrip")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullOrigin_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin(null)
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Origin is required");
    }

    @Test
    void blankOrigin_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("  ")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Origin is required");
    }

    @Test
    void shortOrigin_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("A")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Origin must be at least 2 characters");
    }

    @Test
    void nullDestination_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination(null)
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Destination is required");
    }

    @Test
    void shortDestination_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("M")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Destination must be at least 2 characters");
    }

    @Test
    void nullDepartureDate_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(null)
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Departure date is required");
    }

    @Test
    void pastDepartureDate_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().minusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Departure date cannot be in the past");
    }

    @Test
    void todayDepartureDate_ShouldPassValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now())
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void nullTripType_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType(null)
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Trip type is required");
    }

    @Test
    void invalidTripType_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("invalid")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Trip type must be 'oneway' or 'roundtrip'");
    }

    @Test
    void nullPassengers_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(null)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Number of passengers is required");
    }

    @Test
    void zeroPassengers_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(0)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Minimum 1 passenger required");
    }

    @Test
    void tooManyPassengers_ShouldFailValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(10)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Maximum 9 passengers allowed");
    }

    @Test
    void maxPassengers_ShouldPassValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(9)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void multipleValidationErrors_ShouldReturnAllErrors() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin(null)
                .destination("M")
                .departureDate(LocalDate.now().minusDays(1))
                .tripType("invalid")
                .passengers(0)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(5);

        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(messages).contains(
                "Origin is required",
                "Destination must be at least 2 characters",
                "Departure date cannot be in the past",
                "Trip type must be 'oneway' or 'roundtrip'",
                "Minimum 1 passenger required");
    }

    @Test
    void validOnewayTripWithoutReturnDate_ShouldPassValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .returnDate(null)
                .tripType("oneway")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void validRoundtripWithReturnDate_ShouldPassValidation() {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .returnDate(LocalDate.now().plusDays(7))
                .tripType("roundtrip")
                .passengers(1)
                .build();

        Set<ConstraintViolation<FlightSearchRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
