package com.lp.book.rating.app.service;

import com.lp.book.rating.app.domain.dto.RefreshTokenContainer;
import com.lp.book.rating.app.domain.entity.RefreshToken;
import com.lp.book.rating.app.domain.repository.RefreshTokenRepository;
import com.lp.book.rating.app.exception.TokenValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenService {

    @Value("P14D")
    private Duration refreshTokenTtl;

    private final SecureRandom random = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshTokenContainer issue(@NonNull Long userId) {
        refreshTokenRepository.lockUser(userId);

        //only one refresh token per user is allowed
        refreshTokenRepository.revokeActiveByUserId(userId, "system");

        log.info("Issuing refresh token for user {}, the old one will be revoked", userId);

        var rawToken = createRawToken();
        var refreshToken = createToken(rawToken, userId);

        var token = refreshTokenRepository.save(refreshToken);

        return new RefreshTokenContainer(rawToken, token.getJti());
    }

    private RefreshToken createToken(@NonNull String rawToken, @NonNull Long userId) {
        var hashedToken = calculateHash(rawToken);

        var refreshToken = new RefreshToken();
        var now = Instant.now();

        refreshToken.setJti(UUID.randomUUID());
        refreshToken.setIssuedAt(now);
        refreshToken.setExpiresAt(now.plus(refreshTokenTtl));
        refreshToken.setUserId(userId);
        refreshToken.setHashedToken(hashedToken);
        refreshToken.setRevoked(false);

        return refreshToken;
    }

    private String calculateHash(String opaqueToken) {
        try {
            byte[] tokenBytes = Base64.getUrlDecoder().decode(opaqueToken);
            byte[] digest = sha256(tokenBytes);
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new TokenValidationException("Failed to calculate hash for token", e);
        }
    }

    private static byte[] sha256(byte[] data) {
        try {
            var messageDigest = java.security.MessageDigest.getInstance("SHA-256");
            return messageDigest.digest(data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate SHA-256 hash", e);
        }
    }

    private String createRawToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
