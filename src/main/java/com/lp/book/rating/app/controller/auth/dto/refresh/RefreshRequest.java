package com.lp.book.rating.app.controller.auth.dto.refresh;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@JsonProperty("refresh_token") @NotBlank String refreshToken) {}
