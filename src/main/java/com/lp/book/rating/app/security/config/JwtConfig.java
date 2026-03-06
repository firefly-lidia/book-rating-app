package com.lp.book.rating.app.security.config;

import com.lp.book.rating.app.security.domain.RsaKeys;
import com.lp.book.rating.app.security.service.JwtService;
import com.lp.book.rating.app.service.RefreshTokenService;
import com.lp.book.rating.app.service.UserService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.file.Files;
import java.nio.file.Path;

@EnableConfigurationProperties(JwtProperties.class)
@Configuration
public class JwtConfig {

    @Value("${app.security.jwt.private-pem-path}")
    private String privateKeyPath;

    @Value("${app.security.jwt.issuer}")
    private String issuer;

    @Bean
    public JwtService jwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties,
                                 RefreshTokenService refreshTokenService, UserService userService) {
        return new JwtService(userService, refreshTokenService, jwtEncoder, jwtProperties);
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        var rsaKeys = getRsaKeys();
        var rsaKey = new RSAKey.Builder(rsaKeys.publicKey()).privateKey(rsaKeys.privateKey()).build();

        var jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));

        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        var rsaKeys = getRsaKeys();

        var decoder = NimbusJwtDecoder.withPublicKey(rsaKeys.publicKey()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));

        return decoder;
    }

    private RsaKeys getRsaKeys() {
        try {
            var rsaKey = (RSAKey) JWK.parseFromPEMEncodedObjects(Files.readString(Path.of(privateKeyPath)));

            return new RsaKeys(rsaKey.toRSAPublicKey(), rsaKey.toRSAPrivateKey());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA keys from PEM file", e);
        }
    }

}
