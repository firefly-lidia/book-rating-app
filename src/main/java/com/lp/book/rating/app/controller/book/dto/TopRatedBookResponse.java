package com.lp.book.rating.app.controller.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lp.book.rating.app.domain.enums.BookGenre;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TopRatedBookResponse(
    @NotNull Long id,
    @NotBlank String title,
    @NotBlank BookGenre genre,
    @NotBlank String author,
    @JsonProperty("release_date")@NotBlank LocalDate releaseDate,
    @JsonProperty("avg_score") @NotNull @NotNull @Digits(integer = 10, fraction = 2) BigDecimal avgScore,
    @JsonProperty("ratings_count") @NotNull Long ratingsCount,
    @JsonProperty("rank_score") @NotNull @Digits(integer = 10, fraction = 2) BigDecimal rankScore
) {
}
