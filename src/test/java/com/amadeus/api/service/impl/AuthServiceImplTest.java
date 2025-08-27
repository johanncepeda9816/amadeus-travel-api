package com.amadeus.api.service.impl;

import com.amadeus.api.dto.request.LoginRequest;
import com.amadeus.api.dto.response.LoginResponse;
import com.amadeus.api.dto.response.UserDto;
import com.amadeus.api.entity.User;
import com.amadeus.api.entity.UserRole;
import com.amadeus.api.exception.AuthenticationException;
import com.amadeus.api.repository.UserRepository;
import com.amadeus.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        sampleUser = createSampleUser();
        validLoginRequest = createValidLoginRequest();
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenValidCredentials() {
        String expectedToken = "jwt-token-12345";

        when(userRepository.findByEmailAndEnabledTrue(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), sampleUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(
                eq(sampleUser.getEmail()),
                eq(sampleUser.getId().toString()),
                eq(sampleUser.getRole().name()),
                eq(sampleUser.getName())))
                .thenReturn(expectedToken);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        LoginResponse response = authService.login(validLoginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(expectedToken);
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getId()).isEqualTo(sampleUser.getId());
        assertThat(response.getUser().getEmail()).isEqualTo(sampleUser.getEmail());
        assertThat(response.getUser().getName()).isEqualTo(sampleUser.getName());
        assertThat(response.getUser().getRole()).isEqualTo(sampleUser.getRole().name());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getLastLogin()).isNotNull();

        verify(userRepository).findByEmailAndEnabledTrue(validLoginRequest.getEmail());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), sampleUser.getPassword());
        verify(jwtTokenProvider).generateToken(
                eq(sampleUser.getEmail()),
                eq(sampleUser.getId().toString()),
                eq(sampleUser.getRole().name()),
                eq(sampleUser.getName()));
    }

    @Test
    void login_ShouldThrowAuthenticationException_WhenUserNotFound() {
        when(userRepository.findByEmailAndEnabledTrue(validLoginRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmailAndEnabledTrue(validLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldThrowAuthenticationException_WhenUserIsDisabled() {
        sampleUser.setEnabled(false);

        when(userRepository.findByEmailAndEnabledTrue(validLoginRequest.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmailAndEnabledTrue(validLoginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_ShouldThrowAuthenticationException_WhenPasswordDoesNotMatch() {
        when(userRepository.findByEmailAndEnabledTrue(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), sampleUser.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository).findByEmailAndEnabledTrue(validLoginRequest.getEmail());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), sampleUser.getPassword());
        verify(jwtTokenProvider, never()).generateToken(anyString(), anyString(), anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void logout_ShouldCompleteSuccessfully_WhenValidToken() {
        String validToken = "valid-jwt-token";

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);

        authService.logout(validToken);

        verify(jwtTokenProvider).validateToken(validToken);
    }

    @Test
    void logout_ShouldThrowAuthenticationException_WhenInvalidToken() {
        String invalidToken = "invalid-jwt-token";

        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        assertThatThrownBy(() -> authService.logout(invalidToken))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid token");

        verify(jwtTokenProvider).validateToken(invalidToken);
    }

    @Test
    void getCurrentUser_ShouldReturnUserDto_WhenUserExists() {
        String userEmail = sampleUser.getEmail();

        when(userRepository.findByEmailAndEnabledTrue(userEmail))
                .thenReturn(Optional.of(sampleUser));

        UserDto result = authService.getCurrentUser(userEmail);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sampleUser.getId());
        assertThat(result.getEmail()).isEqualTo(sampleUser.getEmail());
        assertThat(result.getName()).isEqualTo(sampleUser.getName());
        assertThat(result.getRole()).isEqualTo(sampleUser.getRole().name());
        assertThat(result.isEnabled()).isEqualTo(sampleUser.isEnabled());
        assertThat(result.getCreatedAt()).isEqualTo(sampleUser.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(sampleUser.getUpdatedAt());
        assertThat(result.getLastLogin()).isEqualTo(sampleUser.getLastLogin());

        verify(userRepository).findByEmailAndEnabledTrue(userEmail);
    }

    @Test
    void getCurrentUser_ShouldThrowAuthenticationException_WhenUserNotFound() {
        String userEmail = "nonexistent@amadeus.com";

        when(userRepository.findByEmailAndEnabledTrue(userEmail))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(userEmail))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Sesión inválida o usuario inactivo");

        verify(userRepository).findByEmailAndEnabledTrue(userEmail);
    }

    @Test
    void getCurrentUser_ShouldThrowAuthenticationException_WhenUserIsDisabled() {
        String userEmail = "disabled@amadeus.com";
        User disabledUser = createSampleUser();
        disabledUser.setEnabled(false);

        when(userRepository.findByEmailAndEnabledTrue(userEmail))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser(userEmail))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Sesión inválida o usuario inactivo");

        verify(userRepository).findByEmailAndEnabledTrue(userEmail);
    }

    @Test
    void login_ShouldUpdateLastLoginTime() {
        LocalDateTime beforeLogin = LocalDateTime.now().minusMinutes(1);

        when(userRepository.findByEmailAndEnabledTrue(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), sampleUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        authService.login(validLoginRequest);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getLastLogin()).isAfter(beforeLogin);
        assertThat(savedUser.getLastLogin()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void login_ShouldGenerateTokenWithCorrectClaims() {
        when(userRepository.findByEmailAndEnabledTrue(validLoginRequest.getEmail()))
                .thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(validLoginRequest.getPassword(), sampleUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        authService.login(validLoginRequest);

        verify(jwtTokenProvider).generateToken(
                eq(sampleUser.getEmail()),
                eq(sampleUser.getId().toString()),
                eq("ADMIN"),
                eq(sampleUser.getName()));
    }

    private User createSampleUser() {
        return User.builder()
                .id(1L)
                .email("admin@amadeus.com")
                .password("$2a$12$hashedPassword")
                .name("Admin User")
                .role(UserRole.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .lastLogin(LocalDateTime.now().minusHours(2))
                .build();
    }

    private LoginRequest createValidLoginRequest() {
        return LoginRequest.builder()
                .email("admin@amadeus.com")
                .password("password123")
                .build();
    }
}
