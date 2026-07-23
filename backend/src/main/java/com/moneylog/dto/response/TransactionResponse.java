package com.moneylog.dto.response;

import com.moneylog.domain.Transaction;
import com.moneylog.domain.type.TransactionType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

// docs/api-spec.md: 거래 응답 {id, type, amount, categoryId, categoryName, description, transactionDate, createdAt}
@Getter
public class TransactionResponse {

    private final Long id;
    private final TransactionType type;
    private final Long amount;
    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final LocalDate transactionDate;
    private final LocalDateTime createdAt;

    private TransactionResponse(Long id, TransactionType type, Long amount, Long categoryId,
                                 String categoryName, String description, LocalDate transactionDate,
                                 LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getDescription(),
                transaction.getTransactionDate(),
                transaction.getCreatedAt());
    }
}
