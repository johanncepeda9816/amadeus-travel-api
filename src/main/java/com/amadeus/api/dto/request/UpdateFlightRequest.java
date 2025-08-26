package com.amadeus.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFlightRequest {

    @Size(min = 2, max = 10, message = "Flight number must be between 2 and 10 characters")
    private String flightNumber;

    @Size(min = 2, max = 50, message = "Airline must be between 2 and 50 characters")
    private String airline;

    @Size(min = 2, max = 50, message = "Origin must be between 2 and 50 characters")
    private String origin;

    @Size(min = 2, max = 50, message = "Destination must be between 2 and 50 characters")
    private String destination;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    @Pattern(regexp = "^\\d+h \\d+m$", message = "Duration must be in format 'XhYm'")
    private String duration;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @Size(min = 2, max = 50, message = "Aircraft type must be between 2 and 50 characters")
    private String aircraftType;

    @Min(value = 0, message = "Available seats cannot be negative")
    @Max(value = 500, message = "Available seats cannot exceed 500")
    private Integer availableSeats;

    @Pattern(regexp = "^(Economy|Business|First)$", message = "Cabin class must be Economy, Business, or First")
    private String cabinClass;

    private Boolean active;
}
