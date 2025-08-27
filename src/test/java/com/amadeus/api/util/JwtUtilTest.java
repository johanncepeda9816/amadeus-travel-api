package com.amadeus.api.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private HttpServletRequest request;

    @Test
    void extractTokenFromRequest_ShouldReturnToken_WhenValidBearerToken() {
        String expectedToken = "jwt-token-12345";
        String authorizationHeader = "Bearer " + expectedToken;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        String actualToken = JwtUtil.extractTokenFromRequest(request);

        assertThat(actualToken).isEqualTo(expectedToken);
    }

    @Test
    void extractTokenFromRequest_ShouldThrowException_WhenNullAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> JwtUtil.extractTokenFromRequest(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No valid token found");
    }

    @Test
    void extractTokenFromRequest_ShouldThrowException_WhenEmptyAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn("");

        assertThatThrownBy(() -> JwtUtil.extractTokenFromRequest(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No valid token found");
    }

    @Test
    void extractTokenFromRequest_ShouldThrowException_WhenInvalidBearerFormat() {
        when(request.getHeader("Authorization")).thenReturn("Basic token123");

        assertThatThrownBy(() -> JwtUtil.extractTokenFromRequest(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No valid token found");
    }

    @Test
    void extractTokenFromRequest_ShouldReturnEmptyString_WhenBearerWithoutToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        String actualToken = JwtUtil.extractTokenFromRequest(request);

        assertThat(actualToken).isEmpty();
    }

    @Test
    void extractTokenFromRequest_ShouldThrowException_WhenOnlyBearer() {
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        assertThatThrownBy(() -> JwtUtil.extractTokenFromRequest(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No valid token found");
    }

    @Test
    void extractTokenFromRequestOrNull_ShouldReturnToken_WhenValidBearerToken() {
        String expectedToken = "jwt-token-12345";
        String authorizationHeader = "Bearer " + expectedToken;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        String actualToken = JwtUtil.extractTokenFromRequestOrNull(request);

        assertThat(actualToken).isEqualTo(expectedToken);
    }

    @Test
    void extractTokenFromRequestOrNull_ShouldReturnNull_WhenNullAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        String actualToken = JwtUtil.extractTokenFromRequestOrNull(request);

        assertThat(actualToken).isNull();
    }

    @Test
    void extractTokenFromRequestOrNull_ShouldReturnNull_WhenEmptyAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn("");

        String actualToken = JwtUtil.extractTokenFromRequestOrNull(request);

        assertThat(actualToken).isNull();
    }

    @Test
    void extractTokenFromRequestOrNull_ShouldReturnNull_WhenInvalidBearerFormat() {
        when(request.getHeader("Authorization")).thenReturn("Basic token123");

        String actualToken = JwtUtil.extractTokenFromRequestOrNull(request);

        assertThat(actualToken).isNull();
    }

    @Test
    void extractTokenFromRequestOrNull_ShouldReturnEmptyString_WhenBearerWithoutToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        String actualToken = JwtUtil.extractTokenFromRequestOrNull(request);

        assertThat(actualToken).isEmpty();
    }

    @Test
    void extractTokenFromRequestOrNull_ShouldReturnNull_WhenOnlyBearer() {
        when(request.getHeader("Authorization")).thenReturn("Bearer");

        String actualToken = JwtUtil.extractTokenFromRequestOrNull(request);

        assertThat(actualToken).isNull();
    }

    @Test
    void extractTokenFromRequest_ShouldHandleLongToken() {
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String authorizationHeader = "Bearer " + longToken;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        String actualToken = JwtUtil.extractTokenFromRequest(request);

        assertThat(actualToken).isEqualTo(longToken);
    }

    @Test
    void extractTokenFromRequest_ShouldHandleTokenWithSpecialCharacters() {
        String tokenWithSpecialChars = "token-with_special.chars123";
        String authorizationHeader = "Bearer " + tokenWithSpecialChars;

        when(request.getHeader("Authorization")).thenReturn(authorizationHeader);

        String actualToken = JwtUtil.extractTokenFromRequest(request);

        assertThat(actualToken).isEqualTo(tokenWithSpecialChars);
    }

    @Test
    void extractTokenFromRequestOrNull_ShouldHandleBearerWithMultipleSpaces() {
        when(request.getHeader("Authorization")).thenReturn("Bearer  ");

        String actualToken = JwtUtil.extractTokenFromRequestOrNull(request);

        assertThat(actualToken).isEqualTo(" ");
    }

    @Test
    void extractTokenFromRequest_ShouldHandleCaseSensitiveBearer() {
        when(request.getHeader("Authorization")).thenReturn("bearer token123");

        assertThatThrownBy(() -> JwtUtil.extractTokenFromRequest(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No valid token found");
    }
}
