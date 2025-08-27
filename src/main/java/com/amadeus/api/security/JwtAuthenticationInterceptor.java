package com.amadeus.api.security;

import com.amadeus.api.config.SecurityProperties;
import com.amadeus.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;

    public JwtAuthenticationInterceptor(JwtTokenProvider jwtTokenProvider, SecurityProperties securityProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();

        log.info("=== JWT INTERCEPTOR DEBUG ===");
        log.info("Request URI: {}", requestURI);
        log.info("Public endpoints: {}", securityProperties.getPublicEndpoints());

        if (isPublicEndpoint(requestURI)) {
            log.info("Request URI {} is a public endpoint, skipping authentication", requestURI);
            return true;
        }

        log.info("Request URI {} requires authentication", requestURI);
        String token = JwtUtil.extractTokenFromRequestOrNull(request);
        log.info("Token extracted: {}", token != null ? "Present" : "Missing");

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid or missing token for URI: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid or missing token\"}");
            return false;
        }

        String userEmail = jwtTokenProvider.getEmailFromToken(token);
        log.info("Setting userEmail attribute: {}", userEmail);
        request.setAttribute("userEmail", userEmail);

        return true;
    }

    private boolean isPublicEndpoint(String requestURI) {
        return securityProperties.getPublicEndpoints().stream()
                .anyMatch(endpoint -> {
                    if (endpoint.endsWith("/**")) {
                        String basePath = endpoint.substring(0, endpoint.length() - 2);
                        return requestURI.startsWith(basePath);
                    }
                    return requestURI.equals(endpoint);
                });
    }

}
