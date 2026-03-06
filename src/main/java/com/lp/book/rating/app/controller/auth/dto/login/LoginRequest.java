package com.lp.book.rating.app.controller.auth.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(@NotBlank @Email String email, @NotBlank @Size(min = 10, max = 30) String password) {
}
