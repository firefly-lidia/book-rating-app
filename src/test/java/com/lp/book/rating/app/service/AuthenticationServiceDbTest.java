package com.lp.book.rating.app.service;

import com.lp.book.rating.app.annotation.InMemoryTest;
import com.lp.book.rating.app.controller.auth.dto.login.LoginRequest;
import com.lp.book.rating.app.controller.auth.dto.registration.RegisterRequest;
import com.lp.book.rating.app.domain.repository.RefreshTokenRepository;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.UserAlreadyExistsException;
import com.lp.book.rating.app.service.helper.PostgresFlywayHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@InMemoryTest
class AuthenticationServiceDbTest extends PostgresFlywayHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void register_success() {
        var email = "test@gmail.com";
        var name = "test name";
        var surname = "test surname";
        var nickname = "test nickname";
        var age = 25;
        var password = "test password";

        var registerRequest = new RegisterRequest(email, password, nickname, name, surname, (short) age);

        var userId = authenticationService.register(registerRequest);

        assertThat(userId).isNotNull();

        var user = userRepository.findById(userId).orElseThrow();

        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getSurname()).isEqualTo(surname);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getAge()).isEqualTo((short) age);
    }

    @Test
    void register_fail_email_exists() {
        var email = "test@gmail.com";
        var name = "test name";
        var surname = "test surname";
        var nickname = "test nickname";
        var age = 25;
        var password = "test password";

        var registerRequest = new RegisterRequest(email, password, nickname, name, surname, (short) age);

        authenticationService.register(registerRequest);

        var registerRequestDuplicatedEmail = new RegisterRequest(email, password, "nickname", name, surname, (short) age);

        assertThatThrownBy(() -> authenticationService.register(registerRequestDuplicatedEmail))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("User with email test@gmail.com or nickname nickname already exists");

    }

    @Test
    void register_fail_nickname_exists() {
        var email = "test@gmail.com";
        var name = "test name";
        var surname = "test surname";
        var nickname = "test nickname";
        var age = 25;
        var password = "test password";

        var registerRequest = new RegisterRequest(email, password, nickname, name, surname, (short) age);

        authenticationService.register(registerRequest);

        var registerRequestDuplicatedNickname = new RegisterRequest("test1@gmail.com", password, nickname, name, surname, (short) age);

        assertThatThrownBy(() -> authenticationService.register(registerRequestDuplicatedNickname))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage("User with email test1@gmail.com or nickname test nickname already exists");
    }

    @Test
    void login_success() {
        var email = "test@gmail.com";
        var name = "test name";
        var surname = "test surname";
        var nickname = "test nickname";
        var age = 25;
        var password = "test password";

        var registerRequest = new RegisterRequest(email, password, nickname, name, surname, (short) age);

        var userId = authenticationService.register(registerRequest);

        authenticationService.login(new LoginRequest(email,  password));
        var refreshTokens = refreshTokenRepository.findActiveByUser(userId, Instant.now());

        assertThat(refreshTokens).hasSize(1);
        assertThat(refreshTokens.get(0).getRevoked()).isFalse();
    }

    @Test
    void login_failed() {
        var email = "test@gmail.com";
        var name = "test name";
        var surname = "test surname";
        var nickname = "test nickname";
        var age = 25;
        var password = "test password";

        var registerRequest = new RegisterRequest(email, password, nickname, name, surname, (short) age);

        authenticationService.register(registerRequest);

        assertThatThrownBy(() -> authenticationService.login(new LoginRequest(email,  "fake password")))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("Bad credentials");
    }
}