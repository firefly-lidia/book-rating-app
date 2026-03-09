package com.lp.book.rating.app.security.service;

import com.lp.book.rating.app.domain.dto.RefreshTokenContainer;
import com.lp.book.rating.app.domain.dto.UserDto;
import com.lp.book.rating.app.domain.entity.Role;
import com.lp.book.rating.app.exception.InvalidTokenException;
import com.lp.book.rating.app.exception.TokenValidationException;
import com.lp.book.rating.app.security.config.JwtProperties;
import com.lp.book.rating.app.service.RefreshTokenService;
import com.lp.book.rating.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceUTest {

    private static final Long USER_ID = 1l;
    private static final String EMAIL = "ff@email.com";
    private static final String ROLE = "USER";

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtEncoder encoder;

    @Mock
    private JwtProperties properties;

    @Mock
    private Jwt jwt;

    @Mock
    private UserDto userDto;

    @Captor
    private ArgumentCaptor<JwtEncoderParameters> jwtParamsCaptor;

    @InjectMocks
    private JwtService jwtService;

    @Test
    void issue_fail() {
        var errorMessage = "Failed to generate token hash";

        when(refreshTokenService.issue(USER_ID)).thenThrow(new TokenValidationException(errorMessage, new IllegalStateException()));

        assertThatThrownBy(() -> jwtService.issue(EMAIL, ROLE, USER_ID))
                .isInstanceOf(TokenValidationException.class)
                .hasMessage(errorMessage);
    }

    @Test
    void issue_success() {
        var rjti = UUID.randomUUID();
        var refreshToken = "refreshToken";
        var accessToken = "accessToken";
        var issuer = "book-issuer";

        when(properties.issuer()).thenReturn(issuer);
        when(properties.accessTtl()).thenReturn("PT15M");

        when(refreshTokenService.issue(USER_ID)).thenReturn(new RefreshTokenContainer(refreshToken, rjti));
        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn(accessToken);

        var token = jwtService.issue(EMAIL, ROLE, USER_ID);

        assertThat(token.accessToken()).isEqualTo(accessToken);
        assertThat(token.refreshToken()).isEqualTo(refreshToken);

        verify(encoder).encode(jwtParamsCaptor.capture());

        var params = jwtParamsCaptor.getValue();

        var headers = params.getJwsHeader();
        var claims = params.getClaims();

        assertThat(headers.getType()).isEqualTo("JWT");
        assertThat(headers.getAlgorithm().toString()).isEqualTo(SignatureAlgorithm.RS256.getName());

        assertThat(claims.getSubject()).isEqualTo(EMAIL);
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getClaims().get("iss")).isEqualTo(issuer);

        assertThat(claims.getClaims().get("typ")).isEqualTo("access");
        assertThat(claims.getClaims().get("uid")).isEqualTo(USER_ID);
        assertThat((List<String>) claims.getClaims().get("roles")).containsExactly(ROLE);

        var iat = claims.getIssuedAt();
        var exp = claims.getExpiresAt();

        assertThat(iat).isNotNull();
        assertThat(exp).isNotNull();
        assertThat(Duration.between(iat, exp)).isEqualTo(Duration.parse("PT15M"));
    }

    @Test
    void refresh_success() {
        var rjti = UUID.randomUUID();
        var refreshToken = "refreshToken";
        var newRefreshToken = "newRefreshToken";
        var accessToken = "accessToken";
        var issuer = "book-issuer";

        when(properties.issuer()).thenReturn(accessToken);
        when(properties.accessTtl()).thenReturn("PT15M");

        when(refreshTokenService.revoke(refreshToken)).thenReturn(USER_ID);
        when(refreshTokenService.issue(USER_ID)).thenReturn(new RefreshTokenContainer(newRefreshToken, rjti));

        when(userService.findById(USER_ID)).thenReturn(userDto);
        when(userDto.getRole()).thenReturn(Role.USER);
        when(userDto.getEmail()).thenReturn(EMAIL);

        when(encoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn(accessToken);

        var token = jwtService.refresh(refreshToken);

        assertThat(token.accessToken()).isEqualTo(accessToken);
        assertThat(token.refreshToken()).isEqualTo(newRefreshToken);
    }

    @Test
    void refresh_fail() {
        var refreshToken = "refreshToken";
        var errorMessage = "Refresh token has been expired or revoked";

        when(refreshTokenService.revoke(refreshToken)).thenThrow(new InvalidTokenException(errorMessage));

        assertThatThrownBy(() -> jwtService.refresh(refreshToken))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessage(errorMessage);

        verify(refreshTokenService, times(0)).issue(USER_ID);
        verifyNoInteractions(userService);
        verifyNoInteractions(encoder);
    }

    @Test
    void revoke_success() {
        var refreshToken = "refreshToken";

        jwtService.revoke(refreshToken);

        verify(refreshTokenService).revoke(refreshToken);
    }

    @Test
    void revoke_fail() {
        var refreshToken = "refreshToken";
        var errorMessage = "Refresh token has been expired or revoked";

        when(refreshTokenService.revoke(refreshToken)).thenThrow(new InvalidTokenException(errorMessage));

        assertThatThrownBy(() -> jwtService.revoke(refreshToken))
            .isInstanceOf(InvalidTokenException.class)
            .hasMessage(errorMessage);
    }

}
