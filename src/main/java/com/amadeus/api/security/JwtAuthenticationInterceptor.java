package com.amadeus.api.security;

import com.amadeus.api.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;

    public JwtAuthenticationInterceptor(JwtTokenProvider jwtTokenProvider, SecurityProperties securityProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.securityProperties = securityProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        if (isPublicEndpoint(requestURI)) {
            return true;
        }

        String token = JwtUtil.extractTokenFromRequestOrNull(request);
        
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"error\":\"Invalid or missing token\"}");
            return false;
        }

        String userEmail = jwtTokenProvider.getEmailFromToken(token);
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
