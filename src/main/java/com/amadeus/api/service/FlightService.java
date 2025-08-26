package com.amadeus.api.service;

import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.response.FlightSearchResponse;

public interface FlightService {

    FlightSearchResponse searchFlights(FlightSearchRequest request);
}
