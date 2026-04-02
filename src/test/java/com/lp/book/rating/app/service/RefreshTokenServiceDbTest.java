package com.lp.book.rating.app.service;

import com.lp.book.rating.app.annotation.InMemoryTest;
import com.lp.book.rating.app.domain.dto.UserDto;
import com.lp.book.rating.app.domain.repository.RefreshTokenRepository;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.InvalidTokenException;
import com.lp.book.rating.app.service.helper.PostgresFlywayHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@InMemoryTest
class RefreshTokenServiceDbTest extends PostgresFlywayHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Sql(statements = """
                INSERT INTO users (
                    email, password, role, name, surname, nickname, age,
                    rec_version, created_by, created_ts
                ) VALUES (
                    'test@example.com', 'secret-hash', 'USER',
                    'Test', 'User', 'testuser1', 30,
                    1, 'system', now()
                );
            """)
    @Test
    void issue_success() {
        var userId = userRepository.findByEmail("test@example.com").map(UserDto::getId).orElseThrow();

        var token = refreshTokenService.issue(userId);

        assertThat(token).isNotNull();
        assertThat(token.refreshToken()).isNotNull();
        assertThat(token.rjti()).isNotNull();

        var refreshToken = refreshTokenRepository.findByJti(token.rjti()).orElseThrow();

        assertThat(refreshToken.getRevoked()).isFalse();
        assertThat(refreshToken.getExpiresAt()).isAfter(Instant.now());
    }

    @Sql(statements = """
                INSERT INTO users (
                    email, password, role, name, surname, nickname, age,
                    rec_version, created_by, created_ts
                ) VALUES (
                    'test1@example.com', 'secret-hash', 'USER',
                    'Test', 'User', 'testuser2', 30,
                    1, 'system', now()
                );
            """)
    @Test
    void revoke_success() {
        var userId = userRepository.findByEmail("test1@example.com").map(UserDto::getId).orElseThrow();

        var token = refreshTokenService.issue(userId);

        assertThat(token).isNotNull();
        assertThat(token.refreshToken()).isNotNull();
        assertThat(token.rjti()).isNotNull();

        var refreshToken = refreshTokenRepository.findByJti(token.rjti()).orElseThrow();

        assertThat(refreshToken.getRevoked()).isFalse();
        assertThat(refreshToken.getExpiresAt()).isAfter(Instant.now());

        refreshTokenService.revoke(token.refreshToken());

        refreshToken =  refreshTokenRepository.findByJti(token.rjti()).orElseThrow();
        assertThat(refreshToken.getRevoked()).isTrue();
    }

    @Sql(statements = """
                INSERT INTO users (
                    email, password, role, name, surname, nickname, age,
                    rec_version, created_by, created_ts
                ) VALUES (
                    'test2@example.com', 'secret-hash', 'USER',
                    'Test', 'User', 'testuser3', 30,
                    1, 'system', now()
                );
            """)
    @Test
    void revoke_fail_already_revoked() {
        var userId = userRepository.findByEmail("test2@example.com").map(UserDto::getId).orElseThrow();

        var container = refreshTokenService.issue(userId);

        // first revoke succeeds
        refreshTokenService.revoke(container.refreshToken());

        // second revoke should fail
        assertThatThrownBy(() -> refreshTokenService.revoke(container.refreshToken()))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void revoke_fail_token_not_found() {
        var refreshToken = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        assertThatThrownBy(() -> refreshTokenService.revoke(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid refresh token");
    }

}