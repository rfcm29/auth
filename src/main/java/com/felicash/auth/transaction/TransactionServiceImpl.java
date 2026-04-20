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
    private final CategoryRepository categoryRepository;

    public TransactionServiceImpl(FinancialTransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public TransactionResponse createTransaction(CreateTransactionRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Verify category belongs to user
        if (!category.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have permission to use this category");
        }

        FinancialTransaction transaction = FinancialTransaction.builder()
                .amount(request.amount())
                .transactionDate(request.transactionDate())
                .description(request.description())
                .category(category)
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
    public List<TransactionResponse> getTransactionsByUserAndCategoryType(String userEmail, TransactionCategoryType categoryType) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return transactionRepository.findByUserAndCategoryTypeOrderByTransactionDateDesc(user, categoryType)
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
