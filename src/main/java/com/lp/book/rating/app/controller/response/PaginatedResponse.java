package com.lp.book.rating.app.controller.response;

import com.lp.book.rating.app.controller.user.dto.UserResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginatedResponse<T> {

    private T content;
    private PageInfo pageInfo;

    public static <T> PaginatedResponse<T> of(T content, PageInfo pageInfo) {
        var response = new PaginatedResponse<T>();

        response.setContent(content);
        response.setPageInfo(pageInfo);

        return response;
    }
}
