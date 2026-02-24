package com.lp.book.rating.app.util;

import com.lp.book.rating.app.exception.InvalidPaginationCriteriaException;
import com.lp.book.rating.app.exception.InvalidSortingCriteriaException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

@UtilityClass
public class PageSortAndFilterUtils {

    private static final int MAX_ALLOWED_PAGE_SIZE = 10;

    public static PageRequest getPageRequest(int limit, int offset, @NonNull String sort) {
        return PageRequest.of(getPage(limit, offset), limit, getSort(sort));
    }

    private static Sort getSort(@NonNull String sort) {
        try {
            if (sort.isEmpty()) {
                throw new InvalidSortingCriteriaException("Empty sorting expression, remove it or format should be field.direction e.q. title.asc or rating.desc");
            }

            String[] sorting = sort.split("\\.");

            return Sort.by(Sort.Direction.fromString(sorting[sorting.length - 1]), String.join(".", ArrayUtils.subarray(sorting, 0, sorting.length - 1)));
        } catch (Exception ex) {
            throw new InvalidSortingCriteriaException("Invalid sorting expression, format should be field.direction", ex);
        }
    }

    private static int getPage(int limit, int offset) {
        if (limit < 1) {
            throw new InvalidPaginationCriteriaException("Invalid limit, cannot be less than 1");
        }

        if (limit > MAX_ALLOWED_PAGE_SIZE) {
            throw new InvalidPaginationCriteriaException("Invalid limit, cannot be greater than " + MAX_ALLOWED_PAGE_SIZE);
        }

        if (offset < 0) {
            throw new InvalidPaginationCriteriaException("Invalid offset, cannot be less than 0");
        }

        return offset / limit;
    }

}
