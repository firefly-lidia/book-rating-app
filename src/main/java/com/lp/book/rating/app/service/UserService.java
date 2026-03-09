package com.lp.book.rating.app.service;

import com.lp.book.rating.app.controller.user.dto.UserResponse;
import com.lp.book.rating.app.domain.dto.UserDto;
import com.lp.book.rating.app.domain.repository.UserRepository;
import com.lp.book.rating.app.exception.UserNotFoundException;
import com.lp.book.rating.app.util.PageSortAndFilterUtils;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserDto findById(@NonNull Long id) {
        return userRepository.findByid(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAll(int limit, int offset, String sort) {
        var pageRequest = PageSortAndFilterUtils.getPageRequest(limit, offset, sort);

        return userRepository.findAllBy(pageRequest)
            .map(userDto -> new UserResponse(
                userDto.getId(),
                userDto.getEmail(),
                userDto.getRole(),
                userDto.getNickname(),
                userDto.getVersion()
            ));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(@NonNull Long id) {
        return userRepository.findById(id).map(userDto -> new UserResponse(
            userDto.getId(),
            userDto.getEmail(),
            userDto.getRole(),
            userDto.getNickname(),
            userDto.getVersion()
        )).orElseThrow(() -> new UserNotFoundException(id));
    }

}
