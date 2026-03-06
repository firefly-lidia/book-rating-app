package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.auth.dto.login.LoginRequest;
import com.lp.book.rating.app.controller.auth.dto.registration.RegisterRequest;
import com.lp.book.rating.app.domain.entity.Role;
import com.lp.book.rating.app.domain.entity.User;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.UserAlreadyExistsException;
import com.lp.book.rating.app.security.domain.Token;
import com.lp.book.rating.app.security.domain.UserDetails;
import com.lp.book.rating.app.security.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public Long register(RegisterRequest user) {
        if (userRepository.findByEmailOrNickname(user.email(), user.nickname()).isPresent()) {
            throw new UserAlreadyExistsException(user.email(), user.nickname());
        }

        var u = new User();
        u.setEmail(user.email());
        u.setHashedPassword(passwordEncoder.encode(user.password()));
        u.setRole(Role.USER);
        u.setName(user.firstName());
        u.setSurname(user.lastName());
        u.setNickname(user.nickname());
        u.setAge(user.age());

        u = userRepository.save(u);

        return u.getId();
    }

    @Transactional
    public Token login(LoginRequest loginRequest) {
        var usernamePasswordToken = new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
        var authentication = authenticationManager.authenticate(usernamePasswordToken);

        var principal = (UserDetails) authentication.getPrincipal();
        var role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return jwtService.issue(loginRequest.email(), role, principal.getId());
    }


}
