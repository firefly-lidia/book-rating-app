package com.lp.book.rating.app.controller.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenPairResponse(@JsonProperty("access_token") String accessToken,
                                @JsonProperty("refresh_token") String refreshToken) {
}
