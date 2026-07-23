package com.moneylog.dto.response;

import com.moneylog.domain.Category;
import com.moneylog.domain.type.TransactionType;
import lombok.Getter;

// docs/api-spec.md: 카테고리 응답 {id, name, type}
@Getter
public class CategoryResponse {

    private final Long id;
    private final String name;
    private final TransactionType type;

    private CategoryResponse(Long id, String name, TransactionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType());
    }
}
