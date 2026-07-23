package com.moneylog.dto.request;

import com.moneylog.domain.type.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

// docs/api-spec.md: POST/PUT /api/transactions 요청 바디
@Getter
@NoArgsConstructor
public class TransactionRequest {

    @NotNull(message = "거래 타입(INCOME/EXPENSE)은 필수입니다.")
    private TransactionType type;

    @NotNull(message = "금액은 필수입니다.")
    @Positive(message = "금액은 0보다 커야 합니다.")
    private Long amount;

    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;

    private String description;

    @NotNull(message = "거래 날짜는 필수입니다.")
    private LocalDate transactionDate;
}
