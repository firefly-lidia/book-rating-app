package com.lp.book.rating.app.controller.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UserRatingResponse(
    @NotNull Long id,
    @JsonProperty("book_id") @NotNull Long bookId,
    @Min(0) @Max(10) Short score,
    @Size(max = 2000) String description,
    @JsonProperty("created_date") @NotNull LocalDateTime createdDate,
    @NotNull Integer version
) {
}
