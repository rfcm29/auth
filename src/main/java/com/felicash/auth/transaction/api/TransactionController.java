package com.felicash.auth.transaction.api;

import com.felicash.auth.transaction.CreateTransactionRequest;
import com.felicash.auth.transaction.TransactionCategory;
import com.felicash.auth.transaction.TransactionResponse;
import com.felicash.auth.transaction.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Create a new financial transaction
     *
     * @param request Transaction details
     * @param userDetails Authenticated user
     * @return Created transaction
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = transactionService.createTransaction(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all transactions for authenticated user
     * Optionally filter by category
     *
     * @param category Optional category filter (EXPENSE or INCOME)
     * @param userDetails Authenticated user
     * @return List of transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @RequestParam(required = false) TransactionCategory category,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TransactionResponse> transactions;

        if (category != null) {
            transactions = transactionService.getTransactionsByUserAndCategory(
                    userDetails.getUsername(), category);
        } else {
            transactions = transactionService.getAllTransactionsByUser(userDetails.getUsername());
        }

        return ResponseEntity.ok(transactions);
    }

    /**
     * Get a specific transaction by ID
     *
     * @param id Transaction ID
     * @param userDetails Authenticated user
     * @return Transaction details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse transaction = transactionService.getTransactionById(id, userDetails.getUsername());
        return ResponseEntity.ok(transaction);
    }
}
