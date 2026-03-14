package com.lp.book.rating.app.domain.dto;

import com.lp.book.rating.app.domain.enums.BookGenre;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TopRatedBookDto {

    Long getId();
    String getTitle();
    BookGenre getGenre();
    String getAuthor();
    LocalDate getReleaseDate();
    //it is "user-understandable" scores, but it can be not accurate
    BigDecimal getAvgScore();

    Long getRatingsCount();
    // Goodreads like rank score based on weight average formula
    BigDecimal getRankScore();

}
