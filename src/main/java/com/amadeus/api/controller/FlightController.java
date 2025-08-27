package com.amadeus.api.controller;

import com.amadeus.api.dto.ApiResponse;
import com.amadeus.api.dto.request.CreateFlightRequest;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.request.UpdateFlightRequest;
import com.amadeus.api.dto.response.FlightAdminDto;
import com.amadeus.api.dto.response.FlightDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.dto.response.LocationDto;
import com.amadeus.api.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
@Tag(name = "Flights", description = "API for flight management and search")
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "Search flights", description = "Search available flights based on specified criteria", tags = "Flights")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid search parameters", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<FlightSearchResponse>> searchFlights(
            @Parameter(description = "Flight search criteria", required = true) @Valid @RequestBody FlightSearchRequest request) {

        log.info("Flight search request received: {} to {} on {}",
                request.getOrigin(), request.getDestination(), request.getDepartureDate());

        try {
            FlightSearchResponse response = flightService.searchFlights(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Flight search completed successfully"));
        } catch (Exception e) {
            log.error("Error during flight search: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("FLIGHT_SEARCH_ERROR", "Error occurred during flight search"));
        }
    }

    @Operation(summary = "Create flight (Admin)", description = "Creates a new flight. Requires administrator permissions.", security = @SecurityRequirement(name = "Bearer Authentication"), tags = "Flights - Admin")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Flight created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid flight data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Requires ADMIN role")
    })
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightAdminDto>> createFlight(
            @Parameter(description = "Flight data to create", required = true) @Valid @RequestBody CreateFlightRequest request) {

        log.info("Creating new flight: {}", request.getFlightNumber());
        FlightAdminDto flight = flightService.createFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(flight, "Flight created successfully"));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightAdminDto>> getFlightById(@PathVariable Long id) {

        FlightAdminDto flight = flightService.getFlightById(id);
        return ResponseEntity.ok(ApiResponse.success(flight, "Flight retrieved successfully"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<FlightAdminDto>>> getAllFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FlightAdminDto> flights = flightService.getAllFlights(pageable);
        return ResponseEntity.ok(ApiResponse.success(flights, "Flights retrieved successfully"));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightAdminDto>> updateFlight(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFlightRequest request) {

        log.info("Updating flight with id: {}", id);
        FlightAdminDto flight = flightService.updateFlight(id, request);
        return ResponseEntity.ok(ApiResponse.success(flight, "Flight updated successfully"));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFlight(@PathVariable Long id) {

        log.info("Deleting flight with id: {}", id);
        flightService.deleteFlight(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Flight deleted successfully"));
    }

    @GetMapping("/locations/origins")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getAvailableOrigins() {

        List<LocationDto> origins = flightService.getAvailableOrigins();
        return ResponseEntity.ok(ApiResponse.success(origins, "Available origins retrieved successfully"));
    }

    @GetMapping("/locations/destinations")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getAvailableDestinations() {

        List<LocationDto> destinations = flightService.getAvailableDestinations();
        return ResponseEntity.ok(ApiResponse.success(destinations, "Available destinations retrieved successfully"));
    }

    @Operation(summary = "Get all available locations", description = "Returns all available locations as origin and destination", tags = "Flights")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Location list retrieved successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/locations")
    public ResponseEntity<ApiResponse<List<LocationDto>>> getAvailableLocations() {

        List<LocationDto> locations = flightService.getAvailableLocations();
        return ResponseEntity.ok(ApiResponse.success(locations, "Available locations retrieved successfully"));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<FlightDto>>> getUpcomingFlights(
            @RequestParam(defaultValue = "10") int limit) {

        List<FlightDto> flights = flightService.getUpcomingFlights(limit);
        return ResponseEntity.ok(ApiResponse.success(flights, "Upcoming flights retrieved successfully"));
    }
}
