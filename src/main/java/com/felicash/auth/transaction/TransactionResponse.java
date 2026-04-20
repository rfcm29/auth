package com.felicash.auth.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        BigDecimal amount,
        String currency,
        LocalDate transactionDate,
        String description,
        CategoryResponse category,
        Instant createdAt
) {
    public static TransactionResponse from(FinancialTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                CategoryResponse.from(transaction.getCategory()),
                transaction.getCreatedAt()
        );
    }

    public record CategoryResponse(
            UUID id,
            String name,
            TransactionCategoryType type
    ) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getType()
            );
        }
    }
}
