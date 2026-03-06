package com.lp.book.rating.app.security.domain;

import org.springframework.lang.NonNull;

public record Token(@NonNull String accessToken, @NonNull String refreshToken) {
}
