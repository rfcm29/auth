package com.felicash.auth.transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request, String userEmail);

    List<TransactionResponse> getAllTransactionsByUser(String userEmail);

    List<TransactionResponse> getTransactionsByUserAndCategoryType(String userEmail, TransactionCategoryType categoryType);

    TransactionResponse getTransactionById(UUID id, String userEmail);
}
