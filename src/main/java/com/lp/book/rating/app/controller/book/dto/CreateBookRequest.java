package com.lp.book.rating.app.controller.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lp.book.rating.app.domain.enums.BookGenre;
import com.lp.book.rating.app.domain.enums.Currency;
import com.lp.book.rating.app.domain.enums.Language;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBookRequest(
    @Size(max = 500) String title,
    @Size(max = 1000) String description,
    @Size(max = 100) String author,
    @NotNull BookGenre genre,
    @Size(max = 150) String publisher,
    @JsonProperty("release_date") LocalDate releaseDate,
    @NotBlank @ISBN String isbn,
    @NotNull Language language,
    @JsonProperty("number_of_pages") @Max(2500) Integer numberOfPages,
    @DecimalMin("0.00") @Digits(integer = 18, fraction = 2) BigDecimal price,
    @NotNull Currency currency,
    @NotNull Boolean archived
) {
}
