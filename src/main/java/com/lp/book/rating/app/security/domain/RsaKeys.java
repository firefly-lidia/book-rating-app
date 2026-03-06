package com.lp.book.rating.app.security.domain;

import org.springframework.lang.NonNull;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record RsaKeys(@NonNull RSAPublicKey publicKey, @NonNull RSAPrivateKey privateKey) {
}
