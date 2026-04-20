package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import com.felicash.auth.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final FinancialTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionServiceImpl(FinancialTransactionRepository transactionRepository,
                                  UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        FinancialTransaction transaction = FinancialTransaction.builder()
                .amount(request.amount())
                .currency(request.currency())
                .transactionDate(request.transactionDate())
                .referenceId(request.referenceId())
                .parties(request.parties())
                .category(request.category())
                .user(user)
                .build();

        FinancialTransaction saved = transactionRepository.save(transaction);
        return TransactionResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactionsByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return transactionRepository.findByUserOrderByTransactionDateDesc(user)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByUserAndCategory(String userEmail, TransactionCategory category) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return transactionRepository.findByUserAndCategoryOrderByTransactionDateDesc(user, category)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id, String userEmail) {
        FinancialTransaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (!transaction.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have permission to access this transaction");
        }

        return TransactionResponse.from(transaction);
    }
}
