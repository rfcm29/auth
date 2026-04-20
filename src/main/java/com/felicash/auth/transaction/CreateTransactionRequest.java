package com.felicash.auth.transaction;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotNull(message = "Transaction date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        LocalDate transactionDate,

        String description,

        @NotNull(message = "Category is required")
        UUID categoryId
) {}
