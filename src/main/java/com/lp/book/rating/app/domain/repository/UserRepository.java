package com.lp.book.rating.app.domain.repository;

import com.lp.book.rating.app.domain.dto.UserDto;
import com.lp.book.rating.app.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<UserDto> findByEmail(@NonNull String email);

    Page<UserDto> findAllBy(@NonNull Pageable pageable);

    Optional<UserDto> findByEmailOrNickname(@NonNull String email, @NonNull String nickname);

}
