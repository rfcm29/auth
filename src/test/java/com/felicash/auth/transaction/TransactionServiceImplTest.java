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

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Category testCategory;
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

        testCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Food")
                .type(TransactionCategoryType.EXPENSE)
                .user(testUser)
                .build();

        createRequest = new CreateTransactionRequest(
                new BigDecimal("100.00"),
                LocalDate.of(2024, 1, 15),
                "Grocery shopping",
                testCategory.getId()
        );

        savedTransaction = FinancialTransaction.builder()
                .id(UUID.randomUUID())
                .amount(createRequest.amount())
                .transactionDate(createRequest.transactionDate())
                .description(createRequest.description())
                .category(testCategory)
                .user(testUser)
                .build();
    }

    @Test
    void createTransaction_Success() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(FinancialTransaction.class))).thenReturn(savedTransaction);

        // When
        TransactionResponse response = transactionService.createTransaction(createRequest, testUser.getEmail());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo(createRequest.amount());
        assertThat(response.description()).isEqualTo(createRequest.description());
        assertThat(response.category().id()).isEqualTo(testCategory.getId());
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(categoryRepository).findById(testCategory.getId());
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
    void createTransaction_CategoryNotFound_ThrowsException() {
        // Given
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(createRequest, testUser.getEmail()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category not found");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_CategoryDoesNotBelongToUser_ThrowsException() {
        // Given
        User otherUser = User.builder()
                .id(UUID.randomUUID())
                .email("other@example.com")
                .name("Other User")
                .password("password")
                .build();

        Category otherCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Other Category")
                .type(TransactionCategoryType.EXPENSE)
                .user(otherUser)
                .build();

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(otherCategory.getId())).thenReturn(Optional.of(otherCategory));

        CreateTransactionRequest request = new CreateTransactionRequest(
                new BigDecimal("100.00"),
                LocalDate.of(2024, 1, 15),
                "Test",
                otherCategory.getId()
        );

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(request, testUser.getEmail()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You do not have permission to use this category");
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
        assertThat(responses.get(0).description()).isEqualTo("Grocery shopping");
        verify(transactionRepository).findByUserOrderByTransactionDateDesc(testUser);
    }

    @Test
    void getTransactionsByUserAndCategoryType_Success() {
        // Given
        List<FinancialTransaction> transactions = List.of(savedTransaction);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserAndCategoryTypeOrderByTransactionDateDesc(testUser, TransactionCategoryType.EXPENSE))
                .thenReturn(transactions);

        // When
        List<TransactionResponse> responses = transactionService.getTransactionsByUserAndCategoryType(
                testUser.getEmail(), TransactionCategoryType.EXPENSE);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).category().type()).isEqualTo(TransactionCategoryType.EXPENSE);
        verify(transactionRepository).findByUserAndCategoryTypeOrderByTransactionDateDesc(testUser, TransactionCategoryType.EXPENSE);
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
