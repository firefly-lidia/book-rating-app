package com.lp.book.rating.app.domain.dto;

import java.time.Instant;

public interface RefreshTokenDto {
    Long getUserId();
    String getJti();
    Instant getExpiresAt();
    Instant getIssuedAt();
    Boolean getRevoked();
}
