package com.lp.book.rating.app.controller.user;

import com.lp.book.rating.app.controller.handler.GlobalApiExceptionHandler;
import com.lp.book.rating.app.controller.user.dto.UserResponse;
import com.lp.book.rating.app.domain.entity.Role;
import com.lp.book.rating.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

}