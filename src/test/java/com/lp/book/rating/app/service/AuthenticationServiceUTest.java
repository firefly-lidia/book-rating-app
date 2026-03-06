package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.auth.dto.login.LoginRequest;
import com.lp.book.rating.app.controller.auth.dto.registration.RegisterRequest;
import com.lp.book.rating.app.domain.dto.UserDto;
import com.lp.book.rating.app.domain.entity.User;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.TokenValidationException;
import com.lp.book.rating.app.exception.UserAlreadyExistsException;
import com.lp.book.rating.app.security.domain.Token;
import com.lp.book.rating.app.security.domain.UserDetails;
import com.lp.book.rating.app.security.service.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceUTest {

    private static final Long USER_ID = 1L;
    private static final String EMAIL = "test.email@gmail.com";
    private static final String PASSWORD = "password";
    private static final String NICKNAME = "nickname";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDto userDto;

    @Mock
    private RegisterRequest registerRequest;

    @Mock
    private LoginRequest loginRequest;

    @Mock
    private UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;

    @Mock
    private UserDetails userDetails;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_success() {
        when(registerRequest.email()).thenReturn(EMAIL);
        when(registerRequest.password()).thenReturn(PASSWORD);
        when(registerRequest.age()).thenReturn((short) 25);
        when(registerRequest.firstName()).thenReturn("firstname");
        when(registerRequest.lastName()).thenReturn("lastname");
        when(registerRequest.nickname()).thenReturn("nickname");

        when(userRepository.findByEmailOrNickname(EMAIL, NICKNAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("secret_password");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(USER_ID);
            return u;
        });

        var userId = authenticationService.register(registerRequest);
        assertThat(userId).isNotNull();
        assertThat(userId).isEqualTo(USER_ID);

        verify(userRepository).save(userCaptor.capture());

        var storedUser = userCaptor.getValue();

        assertThat(storedUser.getName()).isEqualTo(registerRequest.firstName());
        assertThat(storedUser.getSurname()).isEqualTo(registerRequest.lastName());
        assertThat(storedUser.getNickname()).isEqualTo(registerRequest.nickname());
        assertThat(storedUser.getEmail()).isEqualTo(registerRequest.email());
        assertThat(storedUser.getHashedPassword()).isEqualTo("secret_password");
        assertThat(storedUser.getAge()).isEqualTo(registerRequest.age());
    }

    @Test
    void register_fail() {
        var errorMessage = "User with email " + EMAIL + " or nickname " + NICKNAME + " already exists";

        when(registerRequest.email()).thenReturn(EMAIL);
        when(registerRequest.nickname()).thenReturn(NICKNAME);

        when(userRepository.findByEmailOrNickname(EMAIL, NICKNAME)).thenReturn(Optional.of(userDto));

        assertThatThrownBy(() -> authenticationService.register(registerRequest))
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessage(errorMessage);

        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void login_success() {
        var roleCaptor = ArgumentCaptor.forClass(String.class);
        var userIdCaptor = ArgumentCaptor.forClass(Long.class);

        when(loginRequest.email()).thenReturn(EMAIL);
        when(loginRequest.password()).thenReturn(PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(usernamePasswordAuthenticationToken);
        when(usernamePasswordAuthenticationToken.getPrincipal()).thenReturn(userDetails);

        when(userDetails.getId()).thenReturn(USER_ID);
        when(userDetails.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.issue(anyString(), roleCaptor.capture(), userIdCaptor.capture())).thenReturn(new Token("accessToken", "refreshToken"));

        var token = authenticationService.login(loginRequest);

        assertThat(token.accessToken()).isEqualTo("accessToken");
        assertThat(token.refreshToken()).isEqualTo("refreshToken");

        var role = roleCaptor.getValue();
        assertThat(role).isEqualTo("USER");

        var userId = userIdCaptor.getValue();
        assertThat(userId).isEqualTo(USER_ID);
    }

    @Test
    void login_fail() {
        var errorMessage = "Failed to calculate token hash";

        when(loginRequest.email()).thenReturn(EMAIL);
        when(loginRequest.password()).thenReturn(PASSWORD);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(usernamePasswordAuthenticationToken);
        when(usernamePasswordAuthenticationToken.getPrincipal()).thenReturn(userDetails);

        when(userDetails.getId()).thenReturn(USER_ID);
        when(userDetails.getAuthorities()).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.issue(anyString(), anyString(), anyLong())).thenThrow(new TokenValidationException(errorMessage, new IllegalStateException()));

        assertThatThrownBy(() -> authenticationService.login(loginRequest))
            .isInstanceOf(TokenValidationException.class)
            .hasMessage(errorMessage);
    }

}
