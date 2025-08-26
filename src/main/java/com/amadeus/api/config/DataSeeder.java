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

        log.info("Seeding flights...");

        // Vuelos de BOG a MDE
        LocalDateTime baseDate = LocalDateTime.now().plusDays(30).with(LocalTime.of(8, 0));

        Flight flight1 = Flight.builder()
                .flightNumber("IB6621")
                .airline("Iberia")
                .origin("BOG")
                .destination("MDE")
                .departureTime(baseDate.with(LocalTime.of(8, 30)))
                .arrivalTime(baseDate.with(LocalTime.of(10, 0)))
                .duration("1h 30m")
                .price(new BigDecimal("350000"))
                .aircraftType("Airbus A320")
                .availableSeats(45)
                .cabinClass("Economy")
                .active(true)
                .build();

        Flight flight2 = Flight.builder()
                .flightNumber("VY1234")
                .airline("Vueling")
                .origin("BOG")
                .destination("MDE")
                .departureTime(baseDate.with(LocalTime.of(10, 15)))
                .arrivalTime(baseDate.with(LocalTime.of(11, 40)))
                .duration("1h 25m")
                .price(new BigDecimal("280000"))
                .aircraftType("Airbus A320")
                .availableSeats(32)
                .cabinClass("Economy")
                .active(true)
                .build();

        Flight flight3 = Flight.builder()
                .flightNumber("UX5678")
                .airline("Air Europa")
                .origin("BOG")
                .destination("MDE")
                .departureTime(baseDate.with(LocalTime.of(12, 45)))
                .arrivalTime(baseDate.with(LocalTime.of(14, 20)))
                .duration("1h 35m")
                .price(new BigDecimal("420000"))
                .aircraftType("Airbus A320")
                .availableSeats(28)
                .cabinClass("Economy")
                .active(true)
                .build();

        // Vuelos de MDE a BOG
        Flight flight4 = Flight.builder()
                .flightNumber("IB6623")
                .airline("Iberia")
                .origin("MDE")
                .destination("BOG")
                .departureTime(baseDate.plusDays(5).with(LocalTime.of(9, 15)))
                .arrivalTime(baseDate.plusDays(5).with(LocalTime.of(10, 45)))
                .duration("1h 30m")
                .price(new BigDecimal("360000"))
                .aircraftType("Airbus A320")
                .availableSeats(38)
                .cabinClass("Economy")
                .active(true)
                .build();

        Flight flight5 = Flight.builder()
                .flightNumber("VY2345")
                .airline("Vueling")
                .origin("MDE")
                .destination("BOG")
                .departureTime(baseDate.plusDays(5).with(LocalTime.of(11, 30)))
                .arrivalTime(baseDate.plusDays(5).with(LocalTime.of(12, 55)))
                .duration("1h 25m")
                .price(new BigDecimal("290000"))
                .aircraftType("Airbus A320")
                .availableSeats(41)
                .cabinClass("Economy")
                .active(true)
                .build();

        flightRepository.save(flight1);
        flightRepository.save(flight2);
        flightRepository.save(flight3);
        flightRepository.save(flight4);
        flightRepository.save(flight5);

        log.info("Flights seeded successfully: {} flights created", flightRepository.count());
    }
}
