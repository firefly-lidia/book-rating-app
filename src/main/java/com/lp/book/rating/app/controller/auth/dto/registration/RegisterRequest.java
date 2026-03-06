package com.lp.book.rating.app.controller.auth.dto.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 10, max = 30) String password,
    @NotBlank @Size(min = 1, max = 30) String nickname,
    @NotBlank @Size(min = 1, max = 30) String firstName,
    @NotBlank @Size(min = 1, max = 30) String lastName,
    @NotNull @Min(1) @Max(123) Short age) {
}
