package com.lp.book.rating.app.domain.repository;

import com.lp.book.rating.app.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByHashedToken(@NonNull String hashedToken);

    @Query("""
            select t from RefreshToken t
            where t.userId = :userId
              and t.revoked = false
              and t.expiresAt > :now
            order by t.issuedAt asc
            """)
    List<RefreshToken> findActiveByUser(@NonNull Long userId, @NonNull Instant now);

    @Query(value = "select pg_advisory_xact_lock(:uid)", nativeQuery = true)
    void lockUser(@Param("uid") Long userId);

    @Modifying
    @Query(value = """
            update RefreshToken rt
                set rt.revoked = true,
                    rt.lastModifiedBy = :by,
                    rt.version = rt.version + 1,
                    rt.lastModifiedDate = CURRENT_TIMESTAMP
                where rt.userId = :userId
                    and rt.revoked = false
        """)
    void revokeActiveByUserId(long userId, String by);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            update RefreshToken rt
                set rt.revoked = true,
                    rt.lastModifiedBy = :by,
                    rt.lastModifiedDate = CURRENT_TIMESTAMP,
                    rt.version = rt.version + 1
                where rt.hashedToken = :hash
                    and rt.revoked = false
                    and rt.expiresAt > CURRENT_TIMESTAMP
        """)
    int revokeIfActive(@Param("hash") String hash, @Param("by") String by);

}
