package com.amadeus.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private List<String> publicEndpoints = List.of(
            "/auth/login",
            "/auth/register",
            "/h2-console/**",
            "/actuator/**"
    );

    private List<String> adminEndpoints = List.of(
            "/admin/**",
            "/users/**"
    );

    private List<String> userEndpoints = List.of(
            "/travel/**",
            "/bookings/**"
    );
}
