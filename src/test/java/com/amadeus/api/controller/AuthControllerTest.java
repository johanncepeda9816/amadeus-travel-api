package com.amadeus.api.controller;

import com.amadeus.api.dto.request.LoginRequest;
import com.amadeus.api.dto.response.LoginResponse;
import com.amadeus.api.dto.response.UserDto;
import com.amadeus.api.exception.AuthenticationException;
import com.amadeus.api.service.AuthService;
import com.amadeus.api.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void login_ShouldReturnSuccessResponse_WhenValidCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@amadeus.com")
                .password("password123")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .token("jwt-token")
                .user(LoginResponse.UserDto.builder()
                        .id(1L)
                        .email("admin@amadeus.com")
                        .name("Admin User")
                        .role("ADMIN")
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("admin@amadeus.com"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_ShouldReturnErrorResponse_WhenInvalidCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("admin@amadeus.com")
                .password("wrongpassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("AUTH_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void logout_ShouldReturnSuccessResponse_WhenValidToken() throws Exception {
        String token = "valid-jwt-token";

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(any(HttpServletRequest.class)))
                    .thenReturn(token);

            doNothing().when(authService).logout(token);

            mockMvc.perform(post("/auth/logout")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Logout successful"));

            verify(authService).logout(token);
        }
    }

    @Test
    void logout_ShouldReturnErrorResponse_WhenInvalidToken() throws Exception {
        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(any(HttpServletRequest.class)))
                    .thenThrow(new RuntimeException("No valid token found"));

            mockMvc.perform(post("/auth/logout")
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("AUTH_ERROR"));
        }
    }

    @Test
    void logout_ShouldReturnErrorResponse_WhenAuthServiceThrowsException() throws Exception {
        String token = "valid-jwt-token";

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.extractTokenFromRequest(any(HttpServletRequest.class)))
                    .thenReturn(token);

            doThrow(new AuthenticationException("Invalid token"))
                    .when(authService).logout(token);

            mockMvc.perform(post("/auth/logout")
                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("AUTH_ERROR"))
                    .andExpect(jsonPath("$.message").value("Invalid token"));

            verify(authService).logout(token);
        }
    }

    @Test
    void getCurrentUser_ShouldReturnUserInfo_WhenValidAuthentication() throws Exception {
        String userEmail = "admin@amadeus.com";

        UserDto userDto = UserDto.builder()
                .id(1L)
                .email(userEmail)
                .name("Admin User")
                .role("ADMIN")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .lastLogin(LocalDateTime.now())
                .build();

        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(userEmail);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(authService.getCurrentUser(userEmail)).thenReturn(userDto);

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User information retrieved successfully"))
                    .andExpect(jsonPath("$.data.email").value(userEmail))
                    .andExpect(jsonPath("$.data.role").value("ADMIN"));

            verify(authService).getCurrentUser(userEmail);
        }
    }

    @Test
    void getCurrentUser_ShouldReturnUnauthorized_WhenNoAuthentication() throws Exception {
        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(securityContext.getAuthentication()).thenReturn(null);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("AUTH_ERROR"))
                    .andExpect(jsonPath("$.message").value("Invalid token or user information not available"));
        }
    }

    @Test
    void getCurrentUser_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(authentication.isAuthenticated()).thenReturn(false);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("AUTH_ERROR"));
        }
    }

    @Test
    void getCurrentUser_ShouldReturnUnauthorized_WhenAuthServiceThrowsAuthException() throws Exception {
        String userEmail = "admin@amadeus.com";

        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(userEmail);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(authService.getCurrentUser(anyString()))
                    .thenThrow(new AuthenticationException("Invalid session"));

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("AUTH_ERROR"))
                    .andExpect(jsonPath("$.message").value("Invalid session"));

            verify(authService).getCurrentUser(userEmail);
        }
    }

    @Test
    void getCurrentUser_ShouldReturnInternalServerError_WhenGenericException() throws Exception {
        String userEmail = "admin@amadeus.com";

        try (MockedStatic<SecurityContextHolder> securityContextHolderMock = mockStatic(SecurityContextHolder.class)) {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn(userEmail);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            securityContextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(authService.getCurrentUser(anyString()))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/auth/me"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                    .andExpect(jsonPath("$.message").value("Internal server error"));

            verify(authService).getCurrentUser(userEmail);
        }
    }
}
