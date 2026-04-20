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
        String referenceId,
        String parties,
        TransactionCategory category,
        Instant createdAt
) {
    public static TransactionResponse from(FinancialTransaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionDate(),
                transaction.getReferenceId(),
                transaction.getParties(),
                transaction.getCategory(),
                transaction.getCreatedAt()
        );
    }
}
