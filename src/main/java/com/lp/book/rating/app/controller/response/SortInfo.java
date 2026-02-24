package com.lp.book.rating.app.controller.response;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortInfo {

    @NotNull
    private String field;

    @NotNull
    private String order;

    public static SortInfo of(@NotNull String field, @NotNull String order) {
        SortInfo sortInfo = new SortInfo();

        sortInfo.setField(field);
        sortInfo.setOrder(order);

        return sortInfo;
    }

}
