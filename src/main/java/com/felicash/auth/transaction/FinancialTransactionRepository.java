package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findByUserOrderByTransactionDateDesc(User user);

    @Query("SELECT t FROM FinancialTransaction t WHERE t.user = :user AND t.category.type = :categoryType ORDER BY t.transactionDate DESC")
    List<FinancialTransaction> findByUserAndCategoryTypeOrderByTransactionDateDesc(@Param("user") User user, @Param("categoryType") TransactionCategoryType categoryType);
}
