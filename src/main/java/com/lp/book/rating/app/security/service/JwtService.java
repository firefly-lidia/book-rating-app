package com.lp.book.rating.app.security.service;

import com.lp.book.rating.app.security.config.JwtProperties;
import com.lp.book.rating.app.security.domain.Token;
import com.lp.book.rating.app.service.RefreshTokenService;
import com.lp.book.rating.app.service.UserService;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private static final String CLAIM_TYP = "typ";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_RJTI = "rjti";
    private static final String CLAIM_UID = "uid";

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtEncoder encoder;
    private final JwtProperties properties;

    public JwtService(UserService userService, RefreshTokenService refreshTokenService, JwtEncoder encoder, JwtProperties properties) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.encoder = encoder;
        this.properties = properties;
    }

    public Token issue(@NonNull String email, @NonNull String role, @NonNull Long userId) {
        var refresh = refreshTokenService.issue(userId);
        var access = issueAccess(email, role, refresh.rjti().toString(), userId);
        return new Token(access, refresh.refreshToken());
    }

    public Token refresh(@NonNull String refreshToken) {
        var userId = refreshTokenService.revoke(refreshToken);
        var refresh = refreshTokenService.issue(userId);

        var userInfo = userService.findById(userId);
        var access = issueAccess(userInfo.getEmail(), userInfo.getRole().toString(), refresh.rjti().toString(), userId);

        return new Token(access, refresh.refreshToken());
    }

    public void revoke(@NonNull String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private String issueAccess(String email, String role, String linkedRefreshJti, Long userId) {
        var now = Instant.now();
        var claims = JwtClaimsSet.builder()
            .issuer(properties.issuer())
            .subject(email)
            .issuedAt(now)
            .expiresAt(now.plus(Duration.parse(properties.accessTtl())))
            .id(UUID.randomUUID().toString())
            .claim(CLAIM_TYP, "access")
            .claim(CLAIM_ROLES, List.of(role))
            .claim(CLAIM_RJTI, linkedRefreshJti)
            .claim(CLAIM_UID, userId)
            .build();

        var header = JwsHeader.with(SignatureAlgorithm.RS256)
            .type("JWT")
            .build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
