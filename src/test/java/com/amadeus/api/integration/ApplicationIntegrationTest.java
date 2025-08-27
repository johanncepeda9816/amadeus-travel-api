package com.amadeus.api.integration;

import com.amadeus.api.TravelApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TravelApplication.class)
@ActiveProfiles("test")
class ApplicationIntegrationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void applicationStartsSuccessfully() {
    }
}
