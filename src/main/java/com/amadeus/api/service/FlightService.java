package com.amadeus.api.service;

import com.amadeus.api.dto.request.CreateFlightRequest;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.request.UpdateFlightRequest;
import com.amadeus.api.dto.response.FlightAdminDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {

    FlightSearchResponse searchFlights(FlightSearchRequest request);

    FlightAdminDto createFlight(CreateFlightRequest request);

    FlightAdminDto getFlightById(Long id);

    Page<FlightAdminDto> getAllFlights(Pageable pageable);

    FlightAdminDto updateFlight(Long id, UpdateFlightRequest request);

    void deleteFlight(Long id);
}
