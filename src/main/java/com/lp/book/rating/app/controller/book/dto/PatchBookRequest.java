package com.lp.book.rating.app.controller.book.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lp.book.rating.app.controller.book.annotation.MoneyConsistency;
import com.lp.book.rating.app.domain.enums.BookGenre;
import com.lp.book.rating.app.domain.enums.Currency;
import com.lp.book.rating.app.domain.enums.Language;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_ABSENT)
@MoneyConsistency
public record PatchBookRequest(
    Optional<@Size(max = 500) String> title,
    Optional<@Size(max = 1000) String> description,
    Optional<@Size(max = 100) String> author,
    Optional<BookGenre> genre,
    Optional<@Size(max = 150) String> publisher,
    @JsonProperty("release_date") Optional<LocalDate> releaseDate,
    Optional<@NotBlank @ISBN String> isbn,
    Optional<Language> language,
    @JsonProperty("number_of_pages") Optional<@Max(2500) Integer> numberOfPages,
    Optional<@DecimalMin("0.00") @Digits(integer = 18, fraction = 2) BigDecimal> price,
    Optional<Currency> currency,
    Optional<Boolean> archived
) {
}
