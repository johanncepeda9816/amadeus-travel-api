package com.amadeus.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchResponse {

    private List<FlightDto> outboundFlights;
    private List<FlightDto> returnFlights;
    private SearchMetadata metadata;
}
