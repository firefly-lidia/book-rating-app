package com.lp.book.rating.app.annotation;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockJwtSecurityContextFactory implements WithSecurityContextFactory<WithMockJwt> {

    @Override
    public SecurityContext createSecurityContext(WithMockJwt annotation) {
        var jwt = Jwt.withTokenValue("test")
                .header("alg", "none")
                .claim("uid", annotation.uid())
                .build();

        var auth = new JwtAuthenticationToken(jwt, List.of(), "user-" + annotation.uid());
        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);

        return ctx;
    }

}