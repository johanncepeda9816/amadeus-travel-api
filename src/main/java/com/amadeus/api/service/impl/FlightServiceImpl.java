package com.amadeus.api.service.impl;

import com.amadeus.api.dto.request.CreateFlightRequest;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.request.UpdateFlightRequest;
import com.amadeus.api.dto.response.FlightAdminDto;
import com.amadeus.api.dto.response.FlightDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.dto.response.LocationDto;
import com.amadeus.api.dto.response.SearchMetadata;
import com.amadeus.api.entity.Flight;
import com.amadeus.api.exception.FlightNotFoundException;
import com.amadeus.api.repository.FlightRepository;
import com.amadeus.api.service.FlightService;
import com.amadeus.api.util.LocationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

	private final FlightRepository flightRepository;
	private final LocationMapper locationMapper;

	@Override
	public FlightSearchResponse searchFlights(FlightSearchRequest request) {
		log.info("Searching flights from {} to {} on {}",
				request.getOrigin(), request.getDestination(), request.getDepartureDate());

		List<FlightDto> outboundFlights = searchFlightsFromDatabase(request);
		List<FlightDto> returnFlights = new ArrayList<>();

		if ("roundtrip".equals(request.getTripType()) && request.getReturnDate() != null) {
			returnFlights = searchReturnFlightsFromDatabase(request);
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

	private List<FlightDto> searchFlightsFromDatabase(FlightSearchRequest request) {
		LocalDateTime startOfDay = request.getDepartureDate().atStartOfDay();
		LocalDateTime nextDay = startOfDay.plusDays(1);

		List<Flight> flights = flightRepository.findAvailableFlights(
				request.getOrigin().toUpperCase(),
				request.getDestination().toUpperCase(),
				startOfDay,
				nextDay);

		if (flights.isEmpty()) {
			log.info("No flights found in database for date: {}", request.getDepartureDate());
			return new ArrayList<>();
		}

		log.info("Found {} flights in database", flights.size());
		return flights.stream()
				.map(this::convertToFlightDto)
				.collect(Collectors.toList());
	}

	private List<FlightDto> searchReturnFlightsFromDatabase(FlightSearchRequest request) {
		LocalDateTime startOfDay = request.getReturnDate().atStartOfDay();
		LocalDateTime nextDay = startOfDay.plusDays(1);

		List<Flight> flights = flightRepository.findAvailableFlights(
				request.getDestination().toUpperCase(),
				request.getOrigin().toUpperCase(),
				startOfDay,
				nextDay);

		if (flights.isEmpty()) {
			log.info("No return flights found in database for date: {}", request.getReturnDate());
			return new ArrayList<>();
		}

		log.info("Found {} return flights in database", flights.size());
		return flights.stream()
				.map(this::convertToFlightDto)
				.collect(Collectors.toList());
	}

	private FlightDto convertToFlightDto(Flight flight) {
		return FlightDto.builder()
				.flightNumber(flight.getFlightNumber())
				.airline(flight.getAirline())
				.origin(flight.getOrigin())
				.destination(flight.getDestination())
				.departureTime(flight.getDepartureTime())
				.arrivalTime(flight.getArrivalTime())
				.duration(flight.getDuration())
				.price(flight.getPrice())
				.aircraftType(flight.getAircraftType())
				.availableSeats(flight.getAvailableSeats())
				.cabinClass(flight.getCabinClass())
				.build();
	}

	@Override
	@Transactional
	public FlightAdminDto createFlight(CreateFlightRequest request) {
		validateFlightUniqueness(request.getFlightNumber(), request.getDepartureTime());

		Flight flight = Flight.builder()
				.flightNumber(request.getFlightNumber())
				.airline(request.getAirline())
				.origin(request.getOrigin().toUpperCase())
				.destination(request.getDestination().toUpperCase())
				.departureTime(request.getDepartureTime())
				.arrivalTime(request.getArrivalTime())
				.duration(request.getDuration())
				.price(request.getPrice())
				.aircraftType(request.getAircraftType())
				.availableSeats(request.getAvailableSeats())
				.cabinClass(request.getCabinClass())
				.active(request.getActive())
				.build();

		Flight savedFlight = flightRepository.save(flight);
		log.info("Created new flight: {}", savedFlight.getFlightNumber());

		return convertToFlightAdminDto(savedFlight);
	}

	@Override
	@Transactional(readOnly = true)
	public FlightAdminDto getFlightById(Long id) {
		Flight flight = findFlightById(id);
		return convertToFlightAdminDto(flight);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<FlightAdminDto> getAllFlights(Pageable pageable) {
		return flightRepository.findAll(pageable)
				.map(this::convertToFlightAdminDto);
	}

	@Override
	@Transactional
	public FlightAdminDto updateFlight(Long id, UpdateFlightRequest request) {
		Flight existingFlight = findFlightById(id);

		if (request.getFlightNumber() != null &&
				!request.getFlightNumber().equals(existingFlight.getFlightNumber())) {
			validateFlightUniqueness(request.getFlightNumber(),
					request.getDepartureTime() != null ? request.getDepartureTime()
							: existingFlight.getDepartureTime());
		}

		updateFlightFields(existingFlight, request);
		Flight updatedFlight = flightRepository.save(existingFlight);

		log.info("Updated flight: {}", updatedFlight.getFlightNumber());
		return convertToFlightAdminDto(updatedFlight);
	}

	@Override
	@Transactional
	public void deleteFlight(Long id) {
		Flight flight = findFlightById(id);
		flightRepository.delete(flight);
		log.info("Deleted flight: {}", flight.getFlightNumber());
	}

	private Flight findFlightById(Long id) {
		return flightRepository.findById(id)
				.orElseThrow(() -> new FlightNotFoundException(id));
	}

	private void validateFlightUniqueness(String flightNumber, LocalDateTime departureTime) {
		if (flightRepository.existsByFlightNumberAndDepartureTime(flightNumber, departureTime)) {
			throw new IllegalArgumentException("Flight with number " + flightNumber +
					" already exists for departure time " + departureTime);
		}
	}

	private void updateFlightFields(Flight flight, UpdateFlightRequest request) {
		Optional.ofNullable(request.getFlightNumber()).ifPresent(flight::setFlightNumber);
		Optional.ofNullable(request.getAirline()).ifPresent(flight::setAirline);
		Optional.ofNullable(request.getOrigin()).ifPresent(value -> flight.setOrigin(value.toUpperCase()));
		Optional.ofNullable(request.getDestination()).ifPresent(value -> flight.setDestination(value.toUpperCase()));
		Optional.ofNullable(request.getDepartureTime()).ifPresent(flight::setDepartureTime);
		Optional.ofNullable(request.getArrivalTime()).ifPresent(flight::setArrivalTime);
		Optional.ofNullable(request.getDuration()).ifPresent(flight::setDuration);
		Optional.ofNullable(request.getPrice()).ifPresent(flight::setPrice);
		Optional.ofNullable(request.getAircraftType()).ifPresent(flight::setAircraftType);
		Optional.ofNullable(request.getAvailableSeats()).ifPresent(flight::setAvailableSeats);
		Optional.ofNullable(request.getCabinClass()).ifPresent(flight::setCabinClass);
		Optional.ofNullable(request.getActive()).ifPresent(flight::setActive);
	}

	private FlightAdminDto convertToFlightAdminDto(Flight flight) {
		return FlightAdminDto.builder()
				.id(flight.getId())
				.flightNumber(flight.getFlightNumber())
				.airline(flight.getAirline())
				.origin(flight.getOrigin())
				.destination(flight.getDestination())
				.departureTime(flight.getDepartureTime())
				.arrivalTime(flight.getArrivalTime())
				.duration(flight.getDuration())
				.price(flight.getPrice())
				.aircraftType(flight.getAircraftType())
				.availableSeats(flight.getAvailableSeats())
				.cabinClass(flight.getCabinClass())
				.active(flight.isActive())
				.createdAt(flight.getCreatedAt())
				.updatedAt(flight.getUpdatedAt())
				.build();
	}

	@Override
	@Transactional(readOnly = true)
	public List<LocationDto> getAvailableOrigins() {
		List<String> origins = flightRepository.findDistinctOrigins();
		return origins.stream()
				.map(locationMapper::mapToLocationDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<LocationDto> getAvailableDestinations() {
		List<String> destinations = flightRepository.findDistinctDestinations();
		return destinations.stream()
				.map(locationMapper::mapToLocationDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<LocationDto> getAvailableLocations() {
		List<String> locations = flightRepository.findDistinctLocations();
		return locations.stream()
				.map(locationMapper::mapToLocationDto)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public List<FlightDto> getUpcomingFlights(int limit) {
		List<Flight> flights = flightRepository.findUpcomingFlights(LocalDateTime.now(), PageRequest.of(0, limit));
		return flights.stream().map(this::convertToFlightDto).collect(Collectors.toList());
	}
}
