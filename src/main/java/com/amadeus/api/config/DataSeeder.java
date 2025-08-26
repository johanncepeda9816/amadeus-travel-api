package com.amadeus.api.config;

import com.amadeus.api.entity.Flight;
import com.amadeus.api.entity.User;
import com.amadeus.api.entity.UserRole;
import com.amadeus.api.repository.FlightRepository;
import com.amadeus.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final FlightRepository flightRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) throws Exception {
                seedUsers();
                seedFlights();
        }

        private void seedUsers() {
                if (userRepository.count() > 0) {
                        log.info("Database already has users, skipping seeding");
                        return;
                }

                log.info("Seeding users...");

                User adminUser = User.builder()
                                .email("admin@amadeus.com")
                                .password(passwordEncoder.encode("password123"))
                                .name("Admin User")
                                .role(UserRole.ADMIN)
                                .enabled(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                User regularUser = User.builder()
                                .email("user@amadeus.com")
                                .password(passwordEncoder.encode("password123"))
                                .name("Regular User")
                                .role(UserRole.USER)
                                .enabled(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                userRepository.save(adminUser);
                userRepository.save(regularUser);

                log.info("Users seeded successfully:");
                log.info("Admin: admin@amadeus.com / password123");
                log.info("User: user@amadeus.com / password123");
        }

        private void seedFlights() {
                if (flightRepository.count() > 0) {
                        log.info("Database already has flights, skipping seeding");
                        return;
                }

                log.info("Seeding flights automatically from LocationMapper...");

                List<String> colombianCities = Arrays.asList(
                                "BOGOTA", "MEDELLIN", "CALI", "CARTAGENA", "BARRANQUILLA",
                                "BUCARAMANGA", "PEREIRA", "SANTA_MARTA", "MANIZALES", "VILLAVICENCIO");

                List<String> internationalCities = Arrays.asList(
                                "MADRID", "PARIS", "LONDON", "MIAMI", "NEW_YORK", "MEXICO_CITY", "LIMA", "QUITO");

                List<String> airlines = Arrays.asList(
                                "Avianca", "LATAM", "Viva Air", "Wingo", "Copa Airlines", "Iberia", "Air France",
                                "KLM");

                List<String> aircraftTypes = Arrays.asList(
                                "Airbus A320", "Boeing 737", "Airbus A330", "Boeing 787", "Embraer 190");

                List<Flight> flightsToSave = new ArrayList<>();
                Random random = new Random();
                int flightCounter = 1000;

                for (int day = 1; day <= 60; day++) {
                        LocalDateTime baseDate = LocalDateTime.now().plusDays(day);

                        generateDomesticFlights(colombianCities, airlines, aircraftTypes, flightsToSave,
                                        random, flightCounter, baseDate);

                        if (day % 7 == 0) {
                                generateInternationalFlights(colombianCities, internationalCities, airlines,
                                                aircraftTypes, flightsToSave, random, flightCounter, baseDate);
                        }

                        flightCounter += 50;
                }

                flightRepository.saveAll(flightsToSave);
                log.info("Flights seeded successfully: {} flights created", flightsToSave.size());
        }

        private void generateDomesticFlights(List<String> cities, List<String> airlines,
                        List<String> aircraftTypes, List<Flight> flights,
                        Random random, int flightCounter, LocalDateTime baseDate) {

                for (String origin : cities) {
                        for (String destination : cities) {
                                if (!origin.equals(destination)) {
                                        int flightsPerRoute = 2 + random.nextInt(3);

                                        for (int i = 0; i < flightsPerRoute; i++) {
                                                LocalTime departureTime = LocalTime.of(6 + random.nextInt(16),
                                                                random.nextInt(60));

                                                Flight flight = createFlight(
                                                                generateFlightNumber(
                                                                                airlines.get(random.nextInt(
                                                                                                airlines.size())),
                                                                                flightCounter++),
                                                                airlines.get(random.nextInt(airlines.size())),
                                                                origin,
                                                                destination,
                                                                baseDate.with(departureTime),
                                                                generateDomesticDuration(random),
                                                                generateDomesticPrice(origin, destination, random),
                                                                aircraftTypes.get(random.nextInt(aircraftTypes.size())),
                                                                50 + random.nextInt(150),
                                                                "Economy");

                                                flights.add(flight);
                                        }
                                }
                        }
                }
        }

        private void generateInternationalFlights(List<String> colombianCities, List<String> internationalCities,
                        List<String> airlines, List<String> aircraftTypes,
                        List<Flight> flights, Random random, int flightCounter,
                        LocalDateTime baseDate) {

                List<String> hubCities = Arrays.asList("BOGOTA", "MEDELLIN", "CARTAGENA");

                for (String colombianCity : hubCities) {
                        for (String internationalCity : internationalCities) {
                                if (random.nextDouble() < 0.7) {
                                        LocalTime departureTime = LocalTime.of(random.nextInt(24), random.nextInt(60));

                                        Flight outbound = createFlight(
                                                        generateFlightNumber(
                                                                        airlines.get(random.nextInt(airlines.size())),
                                                                        flightCounter++),
                                                        airlines.get(random.nextInt(airlines.size())),
                                                        colombianCity,
                                                        internationalCity,
                                                        baseDate.with(departureTime),
                                                        generateInternationalDuration(random),
                                                        generateInternationalPrice(internationalCity, random),
                                                        aircraftTypes.get(random.nextInt(aircraftTypes.size())),
                                                        150 + random.nextInt(200),
                                                        "Economy");

                                        Flight returnFlight = createFlight(
                                                        generateFlightNumber(
                                                                        airlines.get(random.nextInt(airlines.size())),
                                                                        flightCounter++),
                                                        outbound.getAirline(),
                                                        internationalCity,
                                                        colombianCity,
                                                        baseDate.plusDays(1).with(departureTime.plusHours(2)),
                                                        outbound.getDuration(),
                                                        outbound.getPrice().add(new BigDecimal(random.nextInt(100000))),
                                                        outbound.getAircraftType(),
                                                        150 + random.nextInt(200),
                                                        "Economy");

                                        flights.add(outbound);
                                        flights.add(returnFlight);
                                }
                        }
                }
        }

        private Flight createFlight(String flightNumber, String airline, String origin, String destination,
                        LocalDateTime departureTime, String duration, BigDecimal price,
                        String aircraftType, int availableSeats, String cabinClass) {

                LocalDateTime arrivalTime = departureTime.plusMinutes(parseDuration(duration));

                return Flight.builder()
                                .flightNumber(flightNumber)
                                .airline(airline)
                                .origin(origin)
                                .destination(destination)
                                .departureTime(departureTime)
                                .arrivalTime(arrivalTime)
                                .duration(duration)
                                .price(price)
                                .aircraftType(aircraftType)
                                .availableSeats(availableSeats)
                                .cabinClass(cabinClass)
                                .active(true)
                                .build();
        }

        private String generateFlightNumber(String airline, int counter) {
                String prefix = switch (airline) {
                        case "Avianca" -> "AV";
                        case "LATAM" -> "LA";
                        case "Viva Air" -> "VV";
                        case "Wingo" -> "P5";
                        case "Copa Airlines" -> "CM";
                        case "Iberia" -> "IB";
                        case "Air France" -> "AF";
                        case "KLM" -> "KL";
                        default -> "XX";
                };
                return prefix + (1000 + (counter % 8999));
        }

        private String generateDomesticDuration(Random random) {
                int hours = 1 + random.nextInt(3);
                int minutes = random.nextInt(60);
                return hours + "h " + minutes + "m";
        }

        private String generateInternationalDuration(Random random) {
                int hours = 8 + random.nextInt(8);
                int minutes = random.nextInt(60);
                return hours + "h " + minutes + "m";
        }

        private BigDecimal generateDomesticPrice(String origin, String destination, Random random) {
                BigDecimal basePrice = new BigDecimal("200000");
                BigDecimal variation = new BigDecimal(random.nextInt(300000));
                return basePrice.add(variation);
        }

        private BigDecimal generateInternationalPrice(String destination, Random random) {
                BigDecimal basePrice = new BigDecimal("1500000");
                BigDecimal variation = new BigDecimal(random.nextInt(2000000));
                return basePrice.add(variation);
        }

        private long parseDuration(String duration) {
                String[] parts = duration.split(" ");
                long hours = Long.parseLong(parts[0].replace("h", ""));
                long minutes = Long.parseLong(parts[1].replace("m", ""));
                return hours * 60 + minutes;
        }
}
