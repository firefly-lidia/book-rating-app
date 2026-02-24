package com.lp.book.rating.app.controller.user.dto;

import com.lp.book.rating.app.domain.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserResponse(@NotNull Long id,
                           @NotBlank @Email String email,
                           Role role,
                           @NotBlank String nickname,
                           @NotNull Integer version) {
}
