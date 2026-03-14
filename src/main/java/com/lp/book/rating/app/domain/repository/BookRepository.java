package com.lp.book.rating.app.domain.repository;

import com.lp.book.rating.app.domain.dto.TopRatedBookDto;
import com.lp.book.rating.app.domain.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByTitleIgnoreCase(@NonNull String title);

    Optional<Book> findByIsbn(@NonNull String isbn);

    Long id(Long id);

    @Query(value = """
            WITH stats AS (
              SELECT COALESCE(AVG(r.score)::numeric, 0) AS C
              FROM rating r
              WHERE r.archived = false
            )
            SELECT
              b.id,
              b.title,
              b.genre,
              b.author,
              b.release_date AS releaseDate,
              COALESCE(AVG(r.score)::float, 0)      AS avgScore,
              COUNT(r.id)                           AS ratingsCount,
              (
                ((COUNT(r.id)::float * COALESCE(AVG(r.score)::float, 0))
                 + ((:m * 1.0) * (SELECT C FROM stats)))
                / ((COUNT(r.id) + :m) * 1.0)
              )                                     AS rankScore
            FROM book b
            JOIN rating r ON r.book_id = b.id
            WHERE b.archived = false AND r.archived = false
            GROUP BY b.id, b.title, b.genre, b.author, b.release_date
            HAVING COUNT(r.id) >= :minVotes
            ORDER BY rankScore DESC, ratingsCount DESC
            """,
        countQuery = """
                    select count(*) from (
                      select 1
                      from book b
                      join rating r on r.book_id = b.id
                      where b.archived = false and r.archived = false
                      group by b.id
                      having count(r.id) >= :minVotes
                    ) t
                    """, nativeQuery = true)
    Page<TopRatedBookDto> findTopRated(int m, int minVotes, @NonNull Pageable pageable);

}
