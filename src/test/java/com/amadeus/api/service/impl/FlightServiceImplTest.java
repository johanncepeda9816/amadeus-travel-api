package com.amadeus.api.service.impl;

import com.amadeus.api.dto.request.CreateFlightRequest;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.request.UpdateFlightRequest;
import com.amadeus.api.dto.response.FlightAdminDto;
import com.amadeus.api.dto.response.FlightDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.dto.response.LocationDto;
import com.amadeus.api.entity.Flight;
import com.amadeus.api.exception.FlightNotFoundException;
import com.amadeus.api.repository.FlightRepository;
import com.amadeus.api.util.LocationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight sampleFlight;
    private FlightSearchRequest searchRequest;
    private CreateFlightRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleFlight = createSampleFlight();
        searchRequest = createSampleSearchRequest();
        createRequest = createSampleCreateRequest();
    }

    @Test
    void searchFlights_ShouldReturnOutboundFlights_WhenOnewayTrip() {
        when(flightRepository.findAvailableFlights(
                eq("BOGOTA"),
                eq("MADRID"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of(sampleFlight));

        FlightSearchResponse response = flightService.searchFlights(searchRequest);

        assertThat(response).isNotNull();
        assertThat(response.getOutboundFlights()).hasSize(1);
        assertThat(response.getReturnFlights()).isEmpty();
        assertThat(response.getMetadata()).isNotNull();
        assertThat(response.getMetadata().getTotalResults()).isEqualTo(1);
        assertThat(response.getMetadata().getCurrency()).isEqualTo("COP");

        verify(flightRepository).findAvailableFlights(
                eq("BOGOTA"),
                eq("MADRID"),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void searchFlights_ShouldReturnBothFlights_WhenRoundTrip() {
        searchRequest.setTripType("roundtrip");
        searchRequest.setReturnDate(LocalDate.now().plusDays(7));

        when(flightRepository.findAvailableFlights(
                eq("BOGOTA"),
                eq("MADRID"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of(sampleFlight));

        when(flightRepository.findAvailableFlights(
                eq("MADRID"),
                eq("BOGOTA"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of(sampleFlight));

        FlightSearchResponse response = flightService.searchFlights(searchRequest);

        assertThat(response).isNotNull();
        assertThat(response.getOutboundFlights()).hasSize(1);
        assertThat(response.getReturnFlights()).hasSize(1);
        assertThat(response.getMetadata().getTotalResults()).isEqualTo(2);

        verify(flightRepository).findAvailableFlights(
                eq("BOGOTA"),
                eq("MADRID"),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
        verify(flightRepository).findAvailableFlights(
                eq("MADRID"),
                eq("BOGOTA"),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void searchFlights_ShouldReturnEmptyList_WhenNoFlightsFound() {
        when(flightRepository.findAvailableFlights(
                anyString(),
                anyString(),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(List.of());

        FlightSearchResponse response = flightService.searchFlights(searchRequest);

        assertThat(response).isNotNull();
        assertThat(response.getOutboundFlights()).isEmpty();
        assertThat(response.getReturnFlights()).isEmpty();
        assertThat(response.getMetadata().getTotalResults()).isZero();
    }

    @Test
    void createFlight_ShouldReturnFlightAdminDto_WhenValidRequest() {
        when(flightRepository.existsByFlightNumberAndDepartureTime(
                createRequest.getFlightNumber(), createRequest.getDepartureTime()))
                .thenReturn(false);

        when(flightRepository.save(any(Flight.class))).thenReturn(sampleFlight);

        FlightAdminDto result = flightService.createFlight(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getFlightNumber()).isEqualTo(createRequest.getFlightNumber());
        assertThat(result.getAirline()).isEqualTo(createRequest.getAirline());
        assertThat(result.getOrigin()).isEqualTo("BOGOTA");
        assertThat(result.getDestination()).isEqualTo("MADRID");

        verify(flightRepository).existsByFlightNumberAndDepartureTime(
                createRequest.getFlightNumber(), createRequest.getDepartureTime());
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void createFlight_ShouldThrowException_WhenFlightAlreadyExists() {
        when(flightRepository.existsByFlightNumberAndDepartureTime(
                createRequest.getFlightNumber(), createRequest.getDepartureTime()))
                .thenReturn(true);

        assertThatThrownBy(() -> flightService.createFlight(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Flight with number")
                .hasMessageContaining("already exists");

        verify(flightRepository).existsByFlightNumberAndDepartureTime(
                createRequest.getFlightNumber(), createRequest.getDepartureTime());
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void getFlightById_ShouldReturnFlightAdminDto_WhenFlightExists() {
        Long flightId = 1L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(sampleFlight));

        FlightAdminDto result = flightService.getFlightById(flightId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sampleFlight.getId());
        assertThat(result.getFlightNumber()).isEqualTo(sampleFlight.getFlightNumber());

        verify(flightRepository).findById(flightId);
    }

    @Test
    void getFlightById_ShouldThrowException_WhenFlightNotFound() {
        Long flightId = 999L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.getFlightById(flightId))
                .isInstanceOf(FlightNotFoundException.class);

        verify(flightRepository).findById(flightId);
    }

    @Test
    void getAllFlights_ShouldReturnPagedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Flight> flights = List.of(sampleFlight);
        Page<Flight> flightPage = new PageImpl<>(flights, pageable, flights.size());

        when(flightRepository.findAll(pageable)).thenReturn(flightPage);

        Page<FlightAdminDto> result = flightService.getAllFlights(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFlightNumber()).isEqualTo(sampleFlight.getFlightNumber());

        verify(flightRepository).findAll(pageable);
    }

    @Test
    void updateFlight_ShouldReturnUpdatedFlight_WhenValidRequest() {
        Long flightId = 1L;
        UpdateFlightRequest updateRequest = UpdateFlightRequest.builder()
                .flightNumber("AV456")
                .price(new BigDecimal("600000"))
                .availableSeats(150)
                .build();

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.existsByFlightNumberAndDepartureTime(
                updateRequest.getFlightNumber(), sampleFlight.getDepartureTime()))
                .thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenReturn(sampleFlight);

        FlightAdminDto result = flightService.updateFlight(flightId, updateRequest);

        assertThat(result).isNotNull();
        verify(flightRepository).findById(flightId);
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void updateFlight_ShouldThrowException_WhenFlightNotFound() {
        Long flightId = 999L;
        UpdateFlightRequest updateRequest = UpdateFlightRequest.builder()
                .flightNumber("AV456")
                .build();

        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.updateFlight(flightId, updateRequest))
                .isInstanceOf(FlightNotFoundException.class);

        verify(flightRepository).findById(flightId);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void updateFlight_ShouldThrowException_WhenFlightNumberAlreadyExists() {
        Long flightId = 1L;
        UpdateFlightRequest updateRequest = UpdateFlightRequest.builder()
                .flightNumber("AV456")
                .build();

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.existsByFlightNumberAndDepartureTime(
                updateRequest.getFlightNumber(), sampleFlight.getDepartureTime()))
                .thenReturn(true);

        assertThatThrownBy(() -> flightService.updateFlight(flightId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Flight with number")
                .hasMessageContaining("already exists");

        verify(flightRepository).findById(flightId);
        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    void deleteFlight_ShouldDeleteFlight_WhenFlightExists() {
        Long flightId = 1L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(sampleFlight));

        flightService.deleteFlight(flightId);

        verify(flightRepository).findById(flightId);
        verify(flightRepository).delete(sampleFlight);
    }

    @Test
    void deleteFlight_ShouldThrowException_WhenFlightNotFound() {
        Long flightId = 999L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> flightService.deleteFlight(flightId))
                .isInstanceOf(FlightNotFoundException.class);

        verify(flightRepository).findById(flightId);
        verify(flightRepository, never()).delete(any(Flight.class));
    }

    @Test
    void getAvailableOrigins_ShouldReturnMappedLocations() {
        List<String> origins = Arrays.asList("BOGOTA", "MADRID");
        LocationDto bogotaDto = LocationDto.builder().code("BOGOTA").name("Bogotá").build();
        LocationDto madridDto = LocationDto.builder().code("MADRID").name("Madrid").build();

        when(flightRepository.findDistinctOrigins()).thenReturn(origins);
        when(locationMapper.mapToLocationDto("BOGOTA")).thenReturn(bogotaDto);
        when(locationMapper.mapToLocationDto("MADRID")).thenReturn(madridDto);

        List<LocationDto> result = flightService.getAvailableOrigins();

        assertThat(result).hasSize(2);
        assertThat(result).contains(bogotaDto, madridDto);

        verify(flightRepository).findDistinctOrigins();
        verify(locationMapper).mapToLocationDto("BOGOTA");
        verify(locationMapper).mapToLocationDto("MADRID");
    }

    @Test
    void getAvailableDestinations_ShouldReturnMappedLocations() {
        List<String> destinations = Arrays.asList("MADRID", "PARIS");
        LocationDto madridDto = LocationDto.builder().code("MADRID").name("Madrid").build();
        LocationDto parisDto = LocationDto.builder().code("PARIS").name("París").build();

        when(flightRepository.findDistinctDestinations()).thenReturn(destinations);
        when(locationMapper.mapToLocationDto("MADRID")).thenReturn(madridDto);
        when(locationMapper.mapToLocationDto("PARIS")).thenReturn(parisDto);

        List<LocationDto> result = flightService.getAvailableDestinations();

        assertThat(result).hasSize(2);
        assertThat(result).contains(madridDto, parisDto);

        verify(flightRepository).findDistinctDestinations();
        verify(locationMapper).mapToLocationDto("MADRID");
        verify(locationMapper).mapToLocationDto("PARIS");
    }

    @Test
    void getAvailableLocations_ShouldReturnAllMappedLocations() {
        List<String> locations = Arrays.asList("BOGOTA", "MADRID", "PARIS");
        LocationDto bogotaDto = LocationDto.builder().code("BOGOTA").name("Bogotá").build();
        LocationDto madridDto = LocationDto.builder().code("MADRID").name("Madrid").build();
        LocationDto parisDto = LocationDto.builder().code("PARIS").name("París").build();

        when(flightRepository.findDistinctLocations()).thenReturn(locations);
        when(locationMapper.mapToLocationDto("BOGOTA")).thenReturn(bogotaDto);
        when(locationMapper.mapToLocationDto("MADRID")).thenReturn(madridDto);
        when(locationMapper.mapToLocationDto("PARIS")).thenReturn(parisDto);

        List<LocationDto> result = flightService.getAvailableLocations();

        assertThat(result).hasSize(3);
        assertThat(result).contains(bogotaDto, madridDto, parisDto);

        verify(flightRepository).findDistinctLocations();
    }

    @Test
    void getUpcomingFlights_ShouldReturnLimitedFlights() {
        int limit = 5;
        List<Flight> flights = List.of(sampleFlight);

        when(flightRepository.findUpcomingFlights(any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(flights);

        List<FlightDto> result = flightService.getUpcomingFlights(limit);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFlightNumber()).isEqualTo(sampleFlight.getFlightNumber());

        verify(flightRepository).findUpcomingFlights(any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    void searchFlightsForAdmin_ShouldReturnAllFlights_WhenSearchTermIsEmpty() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Flight> flights = List.of(sampleFlight);
        Page<Flight> flightPage = new PageImpl<>(flights, pageable, flights.size());

        when(flightRepository.findAll(pageable)).thenReturn(flightPage);

        Page<FlightAdminDto> result = flightService.searchFlightsForAdmin("", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(flightRepository).findAll(pageable);
        verify(flightRepository, never()).searchFlightsByMultipleFields(anyString(), any(Pageable.class));
    }

    @Test
    void searchFlightsForAdmin_ShouldReturnFilteredFlights_WhenSearchTermProvided() {
        String searchTerm = "AV123";
        Pageable pageable = PageRequest.of(0, 20);
        List<Flight> flights = List.of(sampleFlight);
        Page<Flight> flightPage = new PageImpl<>(flights, pageable, flights.size());

        when(flightRepository.searchFlightsByMultipleFields(searchTerm, pageable))
                .thenReturn(flightPage);

        Page<FlightAdminDto> result = flightService.searchFlightsForAdmin(searchTerm, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(flightRepository).searchFlightsByMultipleFields(searchTerm, pageable);
        verify(flightRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void searchFlightsForAdmin_ShouldReturnAllFlights_WhenSearchTermIsNull() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Flight> flights = List.of(sampleFlight);
        Page<Flight> flightPage = new PageImpl<>(flights, pageable, flights.size());

        when(flightRepository.findAll(pageable)).thenReturn(flightPage);

        Page<FlightAdminDto> result = flightService.searchFlightsForAdmin(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(flightRepository).findAll(pageable);
    }

    private Flight createSampleFlight() {
        return Flight.builder()
                .id(1L)
                .flightNumber("AV123")
                .airline("Avianca")
                .origin("BOGOTA")
                .destination("MADRID")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(8))
                .duration("8h 00m")
                .price(new BigDecimal("500000"))
                .aircraftType("Boeing 787")
                .availableSeats(200)
                .cabinClass("Economy")
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FlightSearchRequest createSampleSearchRequest() {
        return FlightSearchRequest.builder()
                .origin("bogota")
                .destination("madrid")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();
    }

    private CreateFlightRequest createSampleCreateRequest() {
        return CreateFlightRequest.builder()
                .flightNumber("AV123")
                .airline("Avianca")
                .origin("bogota")
                .destination("madrid")
                .departureTime(LocalDateTime.now().plusDays(1))
                .arrivalTime(LocalDateTime.now().plusDays(1).plusHours(8))
                .duration("8h 00m")
                .price(new BigDecimal("500000"))
                .aircraftType("Boeing 787")
                .availableSeats(200)
                .cabinClass("Economy")
                .active(true)
                .build();
    }
}
