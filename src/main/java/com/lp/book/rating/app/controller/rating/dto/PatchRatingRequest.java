package com.lp.book.rating.app.controller.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public record PatchRatingRequest(Optional<@Min(0) @Max(10) Short> score,
                                 Optional<@Size(max = 2000) String> description) {
}
