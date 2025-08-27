package com.amadeus.api.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "mySecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm";
    private static final long TEST_EXPIRATION = 86400000L; // 24 hours in milliseconds

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", TEST_EXPIRATION);
    }

    @Test
    void generateToken_ShouldCreateValidToken_WithCorrectClaims() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsNull() {
        boolean isValid = jwtTokenProvider.validateToken(null);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsEmpty() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        JwtTokenProvider expiredTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredTokenProvider, "jwtExpiration", -1000L); // Already expired

        String expiredToken = expiredTokenProvider.generateToken("admin@amadeus.com", "123", "ADMIN", "Admin");

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void getEmailFromToken_ShouldReturnCorrectEmail() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void getUserIdFromToken_ShouldReturnCorrectUserId() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void getRoleFromToken_ShouldReturnCorrectRole() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    void getNameFromToken_ShouldReturnCorrectName() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        String extractedName = jwtTokenProvider.getNameFromToken(token);

        assertThat(extractedName).isEqualTo(name);
    }

    @Test
    void getEmailFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getEmailFromToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getUserIdFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getRoleFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getRoleFromToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void getNameFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        String invalidToken = "invalid.token.here";

        assertThatThrownBy(() -> jwtTokenProvider.getNameFromToken(invalidToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void generateToken_ShouldCreateDifferentTokens_WhenCalledMultipleTimes() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token1 = jwtTokenProvider.generateToken(email, userId, role, name);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtTokenProvider.generateToken(email, userId, role, name);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void generateToken_ShouldHandleSpecialCharacters_InClaims() {
        String email = "user.with+special@amadeus.com";
        String userId = "123-456-789";
        String role = "USER_ADMIN";
        String name = "José María González";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(role);
        assertThat(jwtTokenProvider.getNameFromToken(token)).isEqualTo(name);
    }

    @Test
    void generateToken_ShouldHandleEmptyStrings() {
        String email = "";
        String userId = "";
        String role = "";
        String name = "";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);
        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);
        String extractedName = jwtTokenProvider.getNameFromToken(token);

        if (extractedEmail == null) {
            assertThat(email).isEmpty();
        } else {
            assertThat(extractedEmail).isEqualTo(email);
        }

        if (extractedUserId == null) {
            assertThat(userId).isEmpty();
        } else {
            assertThat(extractedUserId).isEqualTo(userId);
        }

        if (extractedRole == null) {
            assertThat(role).isEmpty();
        } else {
            assertThat(extractedRole).isEqualTo(role);
        }

        if (extractedName == null) {
            assertThat(name).isEmpty();
        } else {
            assertThat(extractedName).isEqualTo(name);
        }
    }

    @Test
    void generateToken_ShouldHandleNullValues() {
        String token = jwtTokenProvider.generateToken(null, null, null, null);

        assertThat(jwtTokenProvider.getEmailFromToken(token)).isNull();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isNull();
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isNull();
        assertThat(jwtTokenProvider.getNameFromToken(token)).isNull();
    }

    @Test
    void generateToken_ShouldCreateTokenWithCorrectExpirationTime() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String token = jwtTokenProvider.generateToken(email, userId, role, name);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getEmailFromToken(token)).isEqualTo(email);
    }

    @Test
    void generateToken_ShouldCreateValidTokenWithAllRoleTypes() {
        String[] roles = { "ADMIN", "USER", "MANAGER", "GUEST" };

        for (String role : roles) {
            String token = jwtTokenProvider.generateToken("test@amadeus.com", "123", role, "Test User");

            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(role);
        }
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsCorrupted() {
        String email = "admin@amadeus.com";
        String userId = "123";
        String role = "ADMIN";
        String name = "Admin User";

        String validToken = jwtTokenProvider.generateToken(email, userId, role, name);
        String corruptedToken = validToken.substring(0, validToken.length() - 5) + "xxxxx";

        boolean isValid = jwtTokenProvider.validateToken(corruptedToken);

        assertThat(isValid).isFalse();
    }
}
