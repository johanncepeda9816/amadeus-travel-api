package com.amadeus.api.controller;

import com.amadeus.api.dto.ApiResponse;
import com.amadeus.api.dto.request.CreateFlightRequest;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.request.UpdateFlightRequest;
import com.amadeus.api.dto.response.FlightAdminDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.service.FlightService;
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

@Slf4j
@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<FlightSearchResponse>> searchFlights(
            @Valid @RequestBody FlightSearchRequest request) {

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

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightAdminDto>> createFlight(
            @Valid @RequestBody CreateFlightRequest request) {

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
}
