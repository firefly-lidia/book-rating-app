package com.lp.book.rating.app.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(@NonNull String issuer,
                            @NonNull String accessTtl,
                            @NonNull String refreshTtl,
                            @NonNull String privatePemPath) {
}
