package com.amadeus.api.controller;

import com.amadeus.api.dto.ApiResponse;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
}
