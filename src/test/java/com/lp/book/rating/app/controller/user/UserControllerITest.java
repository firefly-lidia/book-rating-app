package com.lp.book.rating.app.controller.user;

import com.lp.book.rating.app.controller.handler.GlobalApiExceptionHandler;
import com.lp.book.rating.app.controller.user.dto.UserRatingResponse;
import com.lp.book.rating.app.controller.user.dto.UserResponse;
import com.lp.book.rating.app.domain.entity.Role;
import com.lp.book.rating.app.service.RatingService;
import com.lp.book.rating.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {UserController.class, UserControllerExceptionHandler.class, GlobalApiExceptionHandler.class})
class UserControllerITest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RatingService ratingService;

    @Test
    void getUser_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
            .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getUser_200() throws Exception {
        when(userService.getById(1L)).thenReturn(new UserResponse(1L, "email@email.com", Role.USER, "nickname", 0));

        mockMvc.perform(get("/api/v1/users/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("email@email.com"))
            .andExpect(jsonPath("$.nickname").value("nickname"))
            .andExpect(jsonPath("$.role").value(Role.USER.name()))
            .andExpect(jsonPath("$.version").value(0));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getUser_400_invalid_ID() throws Exception {
        when(userService.getById(1L)).thenReturn(new UserResponse(1L, "email@email.com", Role.USER, "nickname", 0));

        mockMvc.perform(get("/api/v1/users/-1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("400 BAD_REQUEST \"Validation failure\""));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getUser_500_invalid_email() throws Exception {
        when(userService.getById(1L)).thenThrow(new RuntimeException("Unknown runtime exception"));

        mockMvc.perform(get("/api/v1/users/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserRating_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/1/ratings"))
            .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getUserRatings_200() throws Exception {
        var items = List.of(new UserRatingResponse(1L, 1L, (short) 5, "Great book!", LocalDateTime.of(1990, 2, 24, 12, 0), 0));
        when(ratingService.getAllByUserIdAndBookId(1L, 10, 0, "id.asc")).thenReturn(new PageImpl<>(items, PageRequest.of(0, 10), items.size()));

        mockMvc.perform(get("/api/v1/users/1/ratings"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].book_id").value(1))
            .andExpect(jsonPath("$.content[0].score").value("5"))
            .andExpect(jsonPath("$.content[0].description").value("Great book!"))
            .andExpect(jsonPath("$.content[0].created_date").value("1990-02-24T12:00:00"))
            .andExpect(jsonPath("$.content[0].version").value(0));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getUserRatings_400() throws Exception {
        var items = List.of(new UserRatingResponse(1L, 1L, (short) 5, "Great book!", LocalDateTime.of(1990, 2, 24, 12, 0), 0));
        when(ratingService.getAllByUserIdAndBookId(1L, 10, 0, "id.asc")).thenReturn(new PageImpl<>(items, PageRequest.of(0, 10), items.size()));

        mockMvc.perform(get("/api/v1/users/-1/ratings"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("400 BAD_REQUEST \"Validation failure\""));
    }


    @WithMockUser(roles = "ADMIN")
    @Test
    void getUserRatings_500() throws Exception {
        when(ratingService.getAllByUserIdAndBookId(1L, 10, 0, "id.asc")).thenThrow(new RuntimeException("Unknown runtime exception"));

        mockMvc.perform(get("/api/v1/users/1/ratings"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

}