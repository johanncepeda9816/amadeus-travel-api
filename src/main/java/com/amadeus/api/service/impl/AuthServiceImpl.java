package com.amadeus.api.service.impl;

import com.amadeus.api.dto.request.LoginRequest;
import com.amadeus.api.dto.response.LoginResponse;
import com.amadeus.api.dto.response.UserDto;
import com.amadeus.api.entity.User;
import com.amadeus.api.exception.AuthenticationException;
import com.amadeus.api.repository.UserRepository;
import com.amadeus.api.security.JwtTokenProvider;
import com.amadeus.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmailAndEnabledTrue(loginRequest.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(
                user.getEmail(),
                user.getId().toString(),
                user.getRole().name(),
                user.getName());

        return LoginResponse.builder()
                .token(token)
                .user(LoginResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .name(user.getName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }

    @Override
    public void logout(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationException("Invalid token");
        }
    }

    @Override
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmailAndEnabledTrue(email)
                .orElseThrow(() -> new AuthenticationException("Sesión inválida o usuario inactivo"));

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .enabled(user.isEnabled())
                .build();
    }
}
