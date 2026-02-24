package com.lp.book.rating.app.domain.dto;

import com.lp.book.rating.app.domain.entity.Role;

public interface UserDto {

    Long getId();

    String getEmail();

    Role getRole();

    String getHashedPassword();

    String getNickname();

    Integer getVersion();

}
