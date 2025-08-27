package com.amadeus.api.controller;

import com.amadeus.api.dto.request.CreateFlightRequest;
import com.amadeus.api.dto.request.FlightSearchRequest;
import com.amadeus.api.dto.request.UpdateFlightRequest;
import com.amadeus.api.dto.response.FlightAdminDto;
import com.amadeus.api.dto.response.FlightDto;
import com.amadeus.api.dto.response.FlightSearchResponse;
import com.amadeus.api.dto.response.LocationDto;
import com.amadeus.api.dto.response.SearchMetadata;
import com.amadeus.api.service.FlightService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private FlightController flightController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(flightController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void searchFlights_ShouldReturnSuccessResponse_WhenValidRequest() throws Exception {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        FlightDto flightDto = createSampleFlightDto();
        SearchMetadata metadata = SearchMetadata.builder()
                .searchId("search_123")
                .searchTime(LocalDateTime.now())
                .totalResults(1)
                .currency("COP")
                .build();

        FlightSearchResponse response = FlightSearchResponse.builder()
                .outboundFlights(List.of(flightDto))
                .returnFlights(List.of())
                .metadata(metadata)
                .build();

        when(flightService.searchFlights(any(FlightSearchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flight search completed successfully"))
                .andExpect(jsonPath("$.data.outboundFlights").isArray())
                .andExpect(jsonPath("$.data.outboundFlights[0].flightNumber").value("AV123"));

        verify(flightService).searchFlights(any(FlightSearchRequest.class));
    }

    @Test
    void searchFlights_ShouldReturnErrorResponse_WhenServiceThrowsException() throws Exception {
        FlightSearchRequest request = FlightSearchRequest.builder()
                .origin("BOGOTA")
                .destination("MADRID")
                .departureDate(LocalDate.now().plusDays(1))
                .tripType("oneway")
                .passengers(1)
                .build();

        when(flightService.searchFlights(any(FlightSearchRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("FLIGHT_SEARCH_ERROR"));

        verify(flightService).searchFlights(any(FlightSearchRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createFlight_ShouldReturnCreatedStatus_WhenValidRequest() throws Exception {
        CreateFlightRequest request = CreateFlightRequest.builder()
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
                .build();

        FlightAdminDto response = createSampleFlightAdminDto();

        when(flightService.createFlight(any(CreateFlightRequest.class))).thenReturn(response);

        mockMvc.perform(post("/flights/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flight created successfully"))
                .andExpect(jsonPath("$.data.flightNumber").value("AV123"));

        verify(flightService).createFlight(any(CreateFlightRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getFlightById_ShouldReturnFlight_WhenFlightExists() throws Exception {
        Long flightId = 1L;
        FlightAdminDto response = createSampleFlightAdminDto();

        when(flightService.getFlightById(flightId)).thenReturn(response);

        mockMvc.perform(get("/flights/admin/{id}", flightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flight retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(flightService).getFlightById(flightId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllFlights_ShouldReturnPagedFlights() throws Exception {
        List<FlightAdminDto> flights = List.of(createSampleFlightAdminDto());
        Page<FlightAdminDto> page = new PageImpl<>(flights, PageRequest.of(0, 20), flights.size());

        when(flightService.getAllFlights(any())).thenReturn(page);

        mockMvc.perform(get("/flights/admin")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "departureTime")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flights retrieved successfully"))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(flightService).getAllFlights(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void searchFlightsForAdmin_ShouldReturnPagedResults() throws Exception {
        List<FlightAdminDto> flights = List.of(createSampleFlightAdminDto());
        Page<FlightAdminDto> page = new PageImpl<>(flights, PageRequest.of(0, 20), flights.size());

        when(flightService.searchFlightsForAdmin(anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/flights/search/admin")
                .param("searchTerm", "AV123")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(flightService).searchFlightsForAdmin(eq("AV123"), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateFlight_ShouldReturnUpdatedFlight_WhenValidRequest() throws Exception {
        Long flightId = 1L;
        UpdateFlightRequest request = UpdateFlightRequest.builder()
                .flightNumber("AV124")
                .price(new BigDecimal("550000"))
                .build();

        FlightAdminDto response = createSampleFlightAdminDto();
        response.setFlightNumber("AV124");

        when(flightService.updateFlight(eq(flightId), any(UpdateFlightRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/flights/admin/{id}", flightId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flight updated successfully"))
                .andExpect(jsonPath("$.data.flightNumber").value("AV124"));

        verify(flightService).updateFlight(eq(flightId), any(UpdateFlightRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteFlight_ShouldReturnSuccessResponse() throws Exception {
        Long flightId = 1L;

        doNothing().when(flightService).deleteFlight(flightId);

        mockMvc.perform(delete("/flights/admin/{id}", flightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flight deleted successfully"));

        verify(flightService).deleteFlight(flightId);
    }

    @Test
    void getAvailableOrigins_ShouldReturnLocationsList() throws Exception {
        List<LocationDto> origins = Arrays.asList(
                LocationDto.builder().code("BOGOTA").name("Bogotá").build(),
                LocationDto.builder().code("MADRID").name("Madrid").build());

        when(flightService.getAvailableOrigins()).thenReturn(origins);

        mockMvc.perform(get("/flights/locations/origins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Available origins retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").value("BOGOTA"));

        verify(flightService).getAvailableOrigins();
    }

    @Test
    void getAvailableDestinations_ShouldReturnLocationsList() throws Exception {
        List<LocationDto> destinations = Arrays.asList(
                LocationDto.builder().code("MADRID").name("Madrid").build(),
                LocationDto.builder().code("PARIS").name("París").build());

        when(flightService.getAvailableDestinations()).thenReturn(destinations);

        mockMvc.perform(get("/flights/locations/destinations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Available destinations retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());

        verify(flightService).getAvailableDestinations();
    }

    @Test
    void getAvailableLocations_ShouldReturnAllLocations() throws Exception {
        List<LocationDto> locations = Arrays.asList(
                LocationDto.builder().code("BOGOTA").name("Bogotá").build(),
                LocationDto.builder().code("MADRID").name("Madrid").build());

        when(flightService.getAvailableLocations()).thenReturn(locations);

        mockMvc.perform(get("/flights/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Available locations retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());

        verify(flightService).getAvailableLocations();
    }

    @Test
    void getUpcomingFlights_ShouldReturnFlightsList() throws Exception {
        List<FlightDto> flights = List.of(createSampleFlightDto());

        when(flightService.getUpcomingFlights(anyInt())).thenReturn(flights);

        mockMvc.perform(get("/flights/upcoming")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Upcoming flights retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());

        verify(flightService).getUpcomingFlights(10);
    }

    private FlightDto createSampleFlightDto() {
        return FlightDto.builder()
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
                .build();
    }

    private FlightAdminDto createSampleFlightAdminDto() {
        return FlightAdminDto.builder()
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
}
