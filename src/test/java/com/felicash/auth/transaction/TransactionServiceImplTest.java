package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import com.felicash.auth.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private FinancialTransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private CreateTransactionRequest createRequest;
    private FinancialTransaction savedTransaction;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test User")
                .password("password")
                .build();

        createRequest = new CreateTransactionRequest(
                new BigDecimal("100.00"),
                "USD",
                LocalDate.of(2024, 1, 15),
                "TXN-001",
                "John Doe",
                TransactionCategory.EXPENSE
        );

        savedTransaction = FinancialTransaction.builder()
                .id(UUID.randomUUID())
                .amount(createRequest.amount())
                .currency(createRequest.currency())
                .transactionDate(createRequest.transactionDate())
                .referenceId(createRequest.referenceId())
                .parties(createRequest.parties())
                .category(createRequest.category())
                .user(testUser)
                .build();
    }

    @Test
    void createTransaction_Success() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenReturn(savedTransaction);

        // When
        TransactionResponse response = transactionService.createTransaction(createRequest, testUser.getEmail());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo(createRequest.amount());
        assertThat(response.currency()).isEqualTo(createRequest.currency());
        assertThat(response.category()).isEqualTo(createRequest.category());
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(transactionRepository).save(any(FinancialTransaction.class));
    }

    @Test
    void createTransaction_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(createRequest, "nonexistent@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getAllTransactionsByUser_Success() {
        // Given
        List<FinancialTransaction> transactions = List.of(savedTransaction);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserOrderByTransactionDateDesc(testUser)).thenReturn(transactions);

        // When
        List<TransactionResponse> responses = transactionService.getAllTransactionsByUser(testUser.getEmail());

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).referenceId()).isEqualTo("TXN-001");
        verify(transactionRepository).findByUserOrderByTransactionDateDesc(testUser);
    }

    @Test
    void getTransactionsByUserAndCategory_Success() {
        // Given
        List<FinancialTransaction> transactions = List.of(savedTransaction);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserAndCategoryOrderByTransactionDateDesc(testUser, TransactionCategory.EXPENSE))
                .thenReturn(transactions);

        // When
        List<TransactionResponse> responses = transactionService.getTransactionsByUserAndCategory(
                testUser.getEmail(), TransactionCategory.EXPENSE);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).category()).isEqualTo(TransactionCategory.EXPENSE);
        verify(transactionRepository).findByUserAndCategoryOrderByTransactionDateDesc(testUser, TransactionCategory.EXPENSE);
    }

    @Test
    void getTransactionById_Success() {
        // Given
        UUID transactionId = savedTransaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(savedTransaction));

        // When
        TransactionResponse response = transactionService.getTransactionById(transactionId, testUser.getEmail());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(transactionId);
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void getTransactionById_NotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionById(nonExistentId, testUser.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction not found");
    }

    @Test
    void getTransactionById_UnauthorizedAccess_ThrowsException() {
        // Given
        UUID transactionId = savedTransaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(savedTransaction));

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId, "other@example.com"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to access this transaction");
    }
}
