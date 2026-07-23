package com.moneylog.dto.request;

import com.moneylog.domain.type.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// docs/api-spec.md: POST/PUT /api/categories 요청 바디 {name, type}
@Getter
@NoArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    @NotNull(message = "카테고리 타입(INCOME/EXPENSE)은 필수입니다.")
    private TransactionType type;
}
