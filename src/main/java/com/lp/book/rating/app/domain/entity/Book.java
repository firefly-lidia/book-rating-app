package com.lp.book.rating.app.domain.entity;

import com.lp.book.rating.app.domain.enums.BookGenre;
import com.lp.book.rating.app.domain.enums.Currency;
import com.lp.book.rating.app.domain.enums.Language;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.ISBN;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SequenceGenerator(name = "book_id_generator", sequenceName = "book_seq", allocationSize = 1)
@Table(name = "book")
public class Book extends AbstractAuditableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "book_id_generator")
    private Long id;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "genre", nullable = false)
    private BookGenre genre;

    @NotBlank
    @Column(name = "author", nullable = false)
    private String author;

    @NotNull
    @Column(name = "publication_year", nullable = false)
    @Digits(integer = 4, fraction = 0)
    @PositiveOrZero
    private Integer publicationYear;

    @NotBlank
    @Column(name = "isbn", length = 20, nullable = false, unique = true)
    @ISBN
    private String isbn;

    @NotNull
    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @NotNull
    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 2, nullable = false)
    private Language language;

    @NotNull
    @Positive
    @Column(name = "number_of_pages", nullable = false)
    private Integer numberOfPages;

    @NotNull
    @Column(name = "price", precision = 20, scale = 2, nullable = false)
    @Digits(integer = 18, fraction = 2)
    @Positive
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 3, nullable = false)
    private Currency currency;

    @NotNull
    @Column(name = "archived")
    private Boolean archived;

}
