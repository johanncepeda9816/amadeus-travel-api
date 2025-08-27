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
                        "/flights/search",
                        "/flights/locations/**",
                        "/flights/upcoming",
                        // H2
                        "/h2-console/**",
                        "/actuator/**",
                        // DOCS
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api-docs/**");

        private List<String> adminEndpoints = List.of(
                        "/admin/**",
                        "/users/**");

        private List<String> userEndpoints = List.of(
                        "/travel/**",
                        "/bookings/**");
}
