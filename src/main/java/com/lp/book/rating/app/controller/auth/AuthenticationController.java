package com.lp.book.rating.app.controller.auth;

import com.lp.book.rating.app.controller.auth.dto.TokenPairResponse;
import com.lp.book.rating.app.controller.auth.dto.login.LoginRequest;
import com.lp.book.rating.app.controller.auth.dto.refresh.RefreshRequest;
import com.lp.book.rating.app.controller.auth.dto.registration.RegisterRequest;
import com.lp.book.rating.app.controller.auth.dto.registration.RegisterResponse;
import com.lp.book.rating.app.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Valid
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@RequestBody @Valid RegisterRequest request) {
        return new RegisterResponse(authenticationService.register(request));
    }

    @Valid
    @PostMapping("/login")
    public TokenPairResponse login(@RequestBody @Valid LoginRequest request) {
        var token = authenticationService.login(request);

        return new TokenPairResponse(token.accessToken(), token.refreshToken());
    }

    @Valid
    @PostMapping("/refresh")
    public TokenPairResponse refresh(@RequestBody @Valid RefreshRequest refreshRequest) {
        var token = authenticationService.refreshToken(refreshRequest.refreshToken());

        return new TokenPairResponse(token.accessToken(), token.refreshToken());
    }

    @Valid
    @PostMapping("/revoke")
    public ResponseEntity<Void> revoke(@RequestBody @Valid RefreshRequest revokeRequest) {
        authenticationService.revokeToken(revokeRequest.refreshToken());
        return ResponseEntity.noContent().build();
    }

}
