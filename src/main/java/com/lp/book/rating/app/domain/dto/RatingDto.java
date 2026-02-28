package com.lp.book.rating.app.domain.dto;

import java.time.LocalDateTime;

public interface RatingDto {

    Long getId();

    Long getUserId();

    Long getBookId();

    Short getScore();

    String getDescription();

    Integer getVersion();

    LocalDateTime getCreatedDate();

}
