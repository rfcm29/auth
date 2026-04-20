package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findByUserOrderByTransactionDateDesc(User user);

    List<FinancialTransaction> findByUserAndCategoryOrderByTransactionDateDesc(User user, TransactionCategory category);
}
