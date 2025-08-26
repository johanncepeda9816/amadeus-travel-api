package com.amadeus.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {

    @NotBlank(message = "Origin is required")
    @Size(min = 2, message = "Origin must be at least 2 characters")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(min = 2, message = "Destination must be at least 2 characters")
    private String destination;

    @NotNull(message = "Departure date is required")
    @FutureOrPresent(message = "Departure date cannot be in the past")
    private LocalDate departureDate;

    private LocalDate returnDate;

    @NotBlank(message = "Trip type is required")
    @Pattern(regexp = "^(oneway|roundtrip)$", message = "Trip type must be 'oneway' or 'roundtrip'")
    private String tripType;

    @NotNull(message = "Number of passengers is required")
    @Min(value = 1, message = "Minimum 1 passenger required")
    @Max(value = 9, message = "Maximum 9 passengers allowed")
    private Integer passengers;

    @NotNull(message = "Number of rooms is required")
    @Min(value = 1, message = "Minimum 1 room required")
    @Max(value = 5, message = "Maximum 5 rooms allowed")
    private Integer rooms;
}
