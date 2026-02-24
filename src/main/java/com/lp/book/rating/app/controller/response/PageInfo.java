package com.lp.book.rating.app.controller.response;

import com.lp.book.rating.app.exception.InvalidSortingCriteriaException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@ToString
public class PageInfo {

    private int pageNumber;
    private int pageSize;
    private long offset;
    private long totalElements;
    private long totalPages;
    private SortInfo sort;

    public static PageInfo of(Pageable pageable, long totalPages, long totalElements) {
        var pageInfo = new PageInfo();

        pageInfo.setPageNumber(pageable.getPageNumber());
        pageInfo.setPageSize(pageable.getPageSize());
        pageInfo.setOffset(pageable.getOffset());
        pageInfo.setTotalPages(totalPages);
        pageInfo.setTotalElements(totalElements);

        setSorting(pageable, pageInfo);

        return pageInfo;
    }

    private static void setSorting(Pageable pageable, PageInfo pageInfo) {
        if (pageable.getSort().stream().count() > 1) {
            throw new InvalidSortingCriteriaException("Invalid sorting %s - only one field is supported".formatted(pageable.getSort()));
        }

        pageable.getSort().stream()
            .findFirst()
            .ifPresent(order -> pageInfo.setSort(SortInfo.of(order.getProperty(), order.getDirection().name().toLowerCase())));
    }

}
