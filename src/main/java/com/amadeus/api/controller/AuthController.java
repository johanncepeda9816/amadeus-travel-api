package com.amadeus.api.controller;

import com.amadeus.api.dto.ApiResponse;
import com.amadeus.api.dto.request.LoginRequest;
import com.amadeus.api.dto.response.LoginResponse;
import com.amadeus.api.dto.response.UserDto;
import com.amadeus.api.exception.AuthenticationException;
import com.amadeus.api.service.AuthService;
import com.amadeus.api.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for authentication and session management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token", tags = "Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Parameter(description = "Login credentials", required = true) @Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("AUTH_ERROR", e.getMessage()));
        }
    }

    @Operation(summary = "Logout", description = "Invalidates the user's JWT token", security = @SecurityRequirement(name = "Bearer Authentication"), tags = "Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Logout error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        try {
            String token = JwtUtil.extractTokenFromRequest(request);
            authService.logout(token);
            return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("AUTH_ERROR", e.getMessage()));
        }
    }

    @Operation(summary = "Get current user", description = "Returns complete information about the authenticated user including profile details, roles, and session information", security = @SecurityRequirement(name = "Bearer Authentication"), tags = "Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User information retrieved successfully", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid, expired token or inactive session"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
        try {

            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(ApiResponse.error("AUTH_ERROR", "Invalid token or user information not available"));
            }

            String userEmail = authentication.getName();
            UserDto currentUser = authService.getCurrentUser(userEmail);
            return ResponseEntity.ok(ApiResponse.success(currentUser, "User information retrieved successfully"));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("AUTH_ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("INTERNAL_ERROR", "Internal server error"));
        }
    }

}
