package com.lp.book.rating.app.controller.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RatingRequest(@NotNull @Min(0) @Max(10) Short score,
                            @Size(max = 2000) String description) {
}
