package com.lp.book.rating.app.controller.book;

import com.lp.book.rating.app.controller.book.dto.BookResponse;
import com.lp.book.rating.app.controller.handler.GlobalApiExceptionHandler;
import com.lp.book.rating.app.exception.BookNotFoundException;
import com.lp.book.rating.app.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {BookController.class, BookControllerExceptionHandler.class, GlobalApiExceptionHandler.class})
class BookControllerITest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void getBook_401() throws Exception {
        mockMvc.perform(get("/api/v1/books/1"))
            .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getBook_200() throws Exception {
        when(bookService.getById(1L)).thenReturn(new BookResponse(
            1L,
            "title",
            "description",
            "author",
            "mystery",
            "publisher",
            LocalDate.of(2020, 1, 1),
            "978-0-306-40615-7",
            "en",
            100,
            BigDecimal.TEN,
            "CZK",
            false,
            0
        ));

        mockMvc.perform(get("/api/v1/books/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("title"))
            .andExpect(jsonPath("$.description").value("description"))
            .andExpect(jsonPath("$.author").value("author"))
            .andExpect(jsonPath("$.genre").value("mystery"))
            .andExpect(jsonPath("$.publisher").value("publisher"))
            .andExpect(jsonPath("$.release_date").value("2020-01-01"))
            .andExpect(jsonPath("$.isbn").value("978-0-306-40615-7"))
            .andExpect(jsonPath("$.language").value("en"))
            .andExpect(jsonPath("$.number_of_pages").value(100))
            .andExpect(jsonPath("$.price").value(10))
            .andExpect(jsonPath("$.currency").value("CZK"))
            .andExpect(jsonPath("$.archived").value(false))
            .andExpect(jsonPath("$.version").value(0));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getBook_400_invalid_ID() throws Exception {
        when(bookService.getById(1L)).thenReturn(new BookResponse(
            1L,
            "title",
            "description",
            "author",
            "mystery",
            "publisher",
            LocalDate.of(2020, 1, 1),
            "978-0-306-40615-7",
            "en",
            100,
            BigDecimal.TEN,
            "CZK",
            false,
            0
        ));


        mockMvc.perform(get("/api/v1/books/-1"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("getBookById.bookId: must be greater than or equal to 0"));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getBook_500_invalid_email() throws Exception {
        when(bookService.getById(1L)).thenThrow(new RuntimeException("Unknown runtime exception"));

        mockMvc.perform(get("/api/v1/books/1"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getBook_404_bookNotFound() throws Exception {
        when(bookService.getById(1L)).thenThrow(new BookNotFoundException(1L));

        mockMvc.perform(get("/api/v1/books/1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void getBooks_401() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
            .andExpect(status().isUnauthorized());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getBooks_200() throws Exception {
        var items = List.of(new BookResponse(
            1L,
            "title",
            "description",
            "author",
            "mystery",
            "publisher",
            LocalDate.of(2020, 1, 1),
            "978-0-306-40615-7",
            "en",
            100,
            BigDecimal.TEN,
            "CZK",
            false,
            0
        ));
        when(bookService.getAll(10, 0, "releaseDate.asc")).thenReturn(new PageImpl<>(items, PageRequest.of(0, 10), items.size()));

        mockMvc.perform(get("/api/v1/books"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].title").value("title"))
            .andExpect(jsonPath("$.content[0].description").value("description"))
            .andExpect(jsonPath("$.content[0].author").value("author"))
            .andExpect(jsonPath("$.content[0].genre").value("mystery"))
            .andExpect(jsonPath("$.content[0].publisher").value("publisher"))
            .andExpect(jsonPath("$.content[0].release_date").value("2020-01-01"))
            .andExpect(jsonPath("$.content[0].isbn").value("978-0-306-40615-7"))
            .andExpect(jsonPath("$.content[0].language").value("en"))
            .andExpect(jsonPath("$.content[0].number_of_pages").value(100))
            .andExpect(jsonPath("$.content[0].price").value(10))
            .andExpect(jsonPath("$.content[0].currency").value("CZK"))
            .andExpect(jsonPath("$.content[0].archived").value(false))
            .andExpect(jsonPath("$.content[0].version").value(0));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void getBooks_500() throws Exception {
        when(bookService.getAll(10, 0, "releaseDate.asc")).thenThrow(new RuntimeException("Unknown runtime exception"));

        mockMvc.perform(get("/api/v1/books"))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }

}