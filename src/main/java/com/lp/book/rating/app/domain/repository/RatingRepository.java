package com.lp.book.rating.app.domain.repository;

import com.lp.book.rating.app.domain.dto.RatingDto;
import com.lp.book.rating.app.domain.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<RatingDto> findAllByUserId(Long userId, Pageable pageable);

    Optional<Rating> findByBookIdAndUserId(Long bookId, Long userId);

}
