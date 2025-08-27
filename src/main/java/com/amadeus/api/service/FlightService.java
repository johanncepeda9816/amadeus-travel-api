package com.amadeus.api.service;

import com.amadeus.api.dto.request.CreateFlightRequest;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.request.UpdateFlightRequest;
import com.amadeus.api.dto.response.FlightAdminDto;
import com.amadeus.api.dto.response.FlightDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.dto.response.LocationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FlightService {

    FlightSearchResponse searchFlights(FlightSearchRequest request);

    FlightAdminDto createFlight(CreateFlightRequest request);

    FlightAdminDto getFlightById(Long id);

    Page<FlightAdminDto> getAllFlights(Pageable pageable);

    FlightAdminDto updateFlight(Long id, UpdateFlightRequest request);

    void deleteFlight(Long id);

    List<LocationDto> getAvailableOrigins();

    List<LocationDto> getAvailableDestinations();

    List<LocationDto> getAvailableLocations();

    List<FlightDto> getUpcomingFlights(int limit);
}
