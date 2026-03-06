package com.lp.book.rating.app.domain.dto;

import org.springframework.lang.NonNull;

import java.util.UUID;

public record RefreshTokenContainer(@NonNull String refreshToken, @NonNull UUID rjti) {
}
