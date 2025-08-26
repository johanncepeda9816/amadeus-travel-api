package com.amadeus.api.config;

import com.amadeus.api.entity.User;
import com.amadeus.api.entity.UserRole;
import com.amadeus.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
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
}
