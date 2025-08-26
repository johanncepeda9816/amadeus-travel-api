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
public class CreateFlightRequest {

    @NotBlank(message = "Flight number is required")
    @Size(min = 2, max = 10, message = "Flight number must be between 2 and 10 characters")
    private String flightNumber;

    @NotBlank(message = "Airline is required")
    @Size(min = 2, max = 50, message = "Airline must be between 2 and 50 characters")
    private String airline;

    @NotBlank(message = "Origin is required")
    @Size(min = 2, max = 50, message = "Origin must be between 2 and 50 characters")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(min = 2, max = 50, message = "Destination must be between 2 and 50 characters")
    private String destination;

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalDateTime arrivalTime;

    @NotBlank(message = "Duration is required")
    @Pattern(regexp = "^\\d+h \\d+m$", message = "Duration must be in format 'XhYm'")
    private String duration;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format is invalid")
    private BigDecimal price;

    @NotBlank(message = "Aircraft type is required")
    @Size(min = 2, max = 50, message = "Aircraft type must be between 2 and 50 characters")
    private String aircraftType;

    @NotNull(message = "Available seats is required")
    @Min(value = 1, message = "Available seats must be at least 1")
    @Max(value = 500, message = "Available seats cannot exceed 500")
    private Integer availableSeats;

    @NotBlank(message = "Cabin class is required")
    @Pattern(regexp = "^(Economy|Business|First)$", message = "Cabin class must be Economy, Business, or First")
    private String cabinClass;

    @Builder.Default
    private Boolean active = true;
}
