package com.moneylog.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

// docs/api-spec.md 3절: meta.pagination {page, size, totalItems, totalPages, hasNext, hasPrev}
@Getter
public class PageResponse {

    private final int page;
    private final int size;
    private final long totalItems;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrev;

    public PageResponse(int page, int size, long totalItems, int totalPages, boolean hasNext, boolean hasPrev) {
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }

    public static PageResponse of(Page<?> page) {
        return new PageResponse(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious());
    }
}
