package com.lp.book.rating.app.controller.user;

import com.lp.book.rating.app.controller.response.PageInfo;
import com.lp.book.rating.app.controller.response.PaginatedResponse;
import com.lp.book.rating.app.controller.user.dto.UserResponse;
import com.lp.book.rating.app.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users")
public class UserController {

    private static final String DEFAULT_LIMIT = "10";
    private static final String DEFAULT_OFFSET = "0";
    private static final String DEFAULT_SORTING = "id.asc";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Valid
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<PaginatedResponse<List<UserResponse>>> getAll(@RequestParam(required = false, defaultValue = DEFAULT_LIMIT) int limit,
                                                                      @RequestParam(required = false, defaultValue = DEFAULT_OFFSET) int offset,
                                                                      @RequestParam(required = false, defaultValue = DEFAULT_SORTING) String sort) {
        var page = userService.getAll(limit, offset, sort);
        var pageInfo = PageInfo.of(page.getPageable(), page.getTotalPages(), page.getTotalElements());

        return ResponseEntity.ok(PaginatedResponse.of(page.getContent(), pageInfo));
    }

    @Valid
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}")
    public UserResponse get(@PathVariable @Valid @PositiveOrZero Long userId) {
        return userService.getById(userId);
    }

}
