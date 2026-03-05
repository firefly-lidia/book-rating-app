package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.auth.registration.RegisterRequest;
import com.lp.book.rating.app.domain.entity.Role;
import com.lp.book.rating.app.domain.entity.User;
import com.lp.book.rating.app.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long register(RegisterRequest user) {
        if (userRepository.findByEmailOrNickname(user.email(), user.nickname()).isPresent()) {
            throw new IllegalArgumentException("User with the same email or nickname already exists");
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

}
