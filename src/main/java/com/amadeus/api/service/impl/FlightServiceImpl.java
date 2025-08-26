package com.amadeus.api.service.impl;

import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.response.FlightDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.dto.response.SearchMetadata;
import com.amadeus.api.service.FlightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

	@Override
	public FlightSearchResponse searchFlights(FlightSearchRequest request) {
		log.info("Searching flights from {} to {} on {}",
				request.getOrigin(), request.getDestination(), request.getDepartureDate());

		List<FlightDto> outboundFlights = generateOutboundFlights(request);
		List<FlightDto> returnFlights = new ArrayList<>();

		if ("roundtrip".equals(request.getTripType()) && request.getReturnDate() != null) {
			returnFlights = generateReturnFlights(request);
		}

		int totalResults = outboundFlights.size() + returnFlights.size();

		SearchMetadata metadata = SearchMetadata.builder()
				.searchId("search_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10))
				.searchTime(LocalDateTime.now())
				.totalResults(totalResults)
				.currency("COP")
				.build();

		return FlightSearchResponse.builder()
				.outboundFlights(outboundFlights)
				.returnFlights(returnFlights)
				.metadata(metadata)
				.build();
	}

	private List<FlightDto> generateOutboundFlights(FlightSearchRequest request) {
		List<FlightDto> flights = new ArrayList<>();

		String origin = request.getOrigin().toUpperCase();
		String destination = request.getDestination().toUpperCase();
		LocalDateTime departureDate = request.getDepartureDate().atStartOfDay();

		flights.add(createFlight("IB6621", "Iberia", origin, destination,
				departureDate.with(LocalTime.of(8, 30)), "1h 30m", new BigDecimal("350000")));

		flights.add(createFlight("VY1234", "Vueling", origin, destination,
				departureDate.with(LocalTime.of(10, 15)), "1h 25m", new BigDecimal("280000")));

		flights.add(createFlight("UX5678", "Air Europa", origin, destination,
				departureDate.with(LocalTime.of(12, 45)), "1h 35m", new BigDecimal("420000")));

		flights.add(createFlight("FR9012", "Ryanair", origin, destination,
				departureDate.with(LocalTime.of(14, 20)), "1h 20m", new BigDecimal("180000")));

		flights.add(createFlight("IB6622", "Iberia", origin, destination,
				departureDate.with(LocalTime.of(16, 10)), "1h 30m", new BigDecimal("380000")));

		flights.add(createFlight("VY5678", "Vueling", origin, destination,
				departureDate.with(LocalTime.of(20, 30)), "1h 25m", new BigDecimal("320000")));

		return flights;
	}

	private List<FlightDto> generateReturnFlights(FlightSearchRequest request) {
		List<FlightDto> flights = new ArrayList<>();

		String origin = request.getDestination().toUpperCase();
		String destination = request.getOrigin().toUpperCase();
		LocalDateTime returnDate = request.getReturnDate().atStartOfDay();

		flights.add(createFlight("IB6623", "Iberia", origin, destination,
				returnDate.with(LocalTime.of(9, 15)), "1h 30m", new BigDecimal("360000")));

		flights.add(createFlight("VY2345", "Vueling", origin, destination,
				returnDate.with(LocalTime.of(11, 30)), "1h 25m", new BigDecimal("290000")));

		flights.add(createFlight("UX6789", "Air Europa", origin, destination,
				returnDate.with(LocalTime.of(15, 45)), "1h 35m", new BigDecimal("430000")));

		return flights;
	}

	private FlightDto createFlight(String flightNumber, String airline, String origin, String destination,
			LocalDateTime departureTime, String duration, BigDecimal price) {

		LocalDateTime arrivalTime = departureTime.plusMinutes(
				Integer.parseInt(duration.split("h")[0]) * 60 +
						Integer.parseInt(duration.split(" ")[1].replace("m", "")));

		return FlightDto.builder()
				.flightNumber(flightNumber)
				.airline(airline)
				.origin(origin)
				.destination(destination)
				.departureTime(departureTime)
				.arrivalTime(arrivalTime)
				.duration(duration)
				.price(price)
				.aircraftType("Airbus A320")
				.availableSeats((int) (Math.random() * 50) + 10)
				.cabinClass("Economy")
				.build();
	}
}
