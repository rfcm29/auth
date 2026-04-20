package com.felicash.auth.transaction;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be uppercase 3-letter ISO code")
        String currency,

        @NotNull(message = "Transaction date is required")
        @PastOrPresent(message = "Transaction date cannot be in the future")
        LocalDate transactionDate,

        @NotBlank(message = "Reference ID is required")
        String referenceId,

        @NotBlank(message = "Parties involved is required")
        String parties,

        @NotNull(message = "Category is required")
        TransactionCategory category
) {}
