package com.lp.book.rating.app.controller.book.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookResponse(
    @NotNull Long id,
    @Size(max = 500) String title,
    @Size(max = 1000) String description,
    @Size(max = 100) String author,
    @Size(max = 100) String genre,
    @Size(max = 150) String publisher,
    @JsonProperty("release_date") LocalDate releaseDate,
    @NotBlank @ISBN String isbn,
    @Size(max = 2) String language,
    @JsonProperty("number_of_pages") @Max(2500) Integer numberOfPages,
    @DecimalMin("0.00") @Digits(integer = 18, fraction = 2) BigDecimal price,
    @Pattern(regexp = "^[A-Z]{3}$") String currency,
    Boolean archived,
    @NotNull Integer version
) {
}
