package com.felicash.auth.shared;

import com.felicash.auth.transaction.Category;
import com.felicash.auth.transaction.CategoryRepository;
import com.felicash.auth.transaction.FinancialTransaction;
import com.felicash.auth.transaction.FinancialTransactionRepository;
import com.felicash.auth.transaction.TransactionCategoryType;
import com.felicash.auth.user.Role;
import com.felicash.auth.user.User;
import com.felicash.auth.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           FinancialTransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Default data already present — skipping seeding.");
            return;
        }

        log.info("Seeding default data...");

        // ── Default admin user ────────────────────────────────────────────────
        User admin = User.builder()
                .name("Admin")
                .email("admin@felicash.com")
                .password(passwordEncoder.encode("Admin@123"))
                .roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
                .build();
        userRepository.save(admin);

        // ── Default demo user ─────────────────────────────────────────────────
        User demo = User.builder()
                .name("Demo User")
                .email("demo@felicash.com")
                .password(passwordEncoder.encode("Demo@123"))
                .build();
        userRepository.save(demo);

        // ── Default categories for admin ──────────────────────────────────────
        List<Category> adminCategories = List.of(
                buildCategory("Salary",       TransactionCategoryType.INCOME,  admin),
                buildCategory("Freelance",    TransactionCategoryType.INCOME,  admin),
                buildCategory("Investments",  TransactionCategoryType.INCOME,  admin),
                buildCategory("Housing",      TransactionCategoryType.EXPENSE, admin),
                buildCategory("Food",         TransactionCategoryType.EXPENSE, admin),
                buildCategory("Transport",    TransactionCategoryType.EXPENSE, admin),
                buildCategory("Health",       TransactionCategoryType.EXPENSE, admin),
                buildCategory("Education",    TransactionCategoryType.EXPENSE, admin),
                buildCategory("Entertainment",TransactionCategoryType.EXPENSE, admin)
        );
        categoryRepository.saveAll(adminCategories);

        // ── Default categories for demo user ──────────────────────────────────
        List<Category> demoCategories = List.of(
                buildCategory("Salary",       TransactionCategoryType.INCOME,  demo),
                buildCategory("Freelance",    TransactionCategoryType.INCOME,  demo),
                buildCategory("Housing",      TransactionCategoryType.EXPENSE, demo),
                buildCategory("Food",         TransactionCategoryType.EXPENSE, demo),
                buildCategory("Transport",    TransactionCategoryType.EXPENSE, demo)
        );
        categoryRepository.saveAll(demoCategories);

        // ── Default transactions for demo user ────────────────────────────────
        Category demoSalary    = findCategory(demoCategories, "Salary");
        Category demoFreelance = findCategory(demoCategories, "Freelance");
        Category demoFood      = findCategory(demoCategories, "Food");
        Category demoTransport = findCategory(demoCategories, "Transport");
        Category demoHousing   = findCategory(demoCategories, "Housing");

        LocalDate today = LocalDate.now();

        List<FinancialTransaction> transactions = List.of(
                buildTransaction(new BigDecimal("3500.00"), "EUR", today.minusDays(15), "Monthly salary",              demoSalary,    demo),
                buildTransaction(new BigDecimal("800.00"),  "EUR", today.minusDays(10), "Freelance project payment",   demoFreelance, demo),
                buildTransaction(new BigDecimal("1200.00"), "EUR", today.minusDays(5),  "Rent payment",                demoHousing,   demo),
                buildTransaction(new BigDecimal("250.00"),  "EUR", today.minusDays(4),  "Supermarket weekly shop",     demoFood,      demo),
                buildTransaction(new BigDecimal("60.00"),   "EUR", today.minusDays(3),  "Monthly transport pass",      demoTransport, demo),
                buildTransaction(new BigDecimal("45.50"),   "EUR", today.minusDays(2),  "Restaurant dinner",           demoFood,      demo),
                buildTransaction(new BigDecimal("30.00"),   "EUR", today.minusDays(1),  "Taxi ride",                   demoTransport, demo)
        );
        transactionRepository.saveAll(transactions);

        log.info("Default data seeded successfully. Admin: admin@felicash.com / Admin@123 | Demo: demo@felicash.com / Demo@123");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Category buildCategory(String name, TransactionCategoryType type, User user) {
        return Category.builder()
                .name(name)
                .type(type)
                .user(user)
                .build();
    }

    private FinancialTransaction buildTransaction(BigDecimal amount, String currency,
                                                   LocalDate date, String description,
                                                   Category category, User user) {
        return FinancialTransaction.builder()
                .amount(amount)
                .currency(currency)
                .transactionDate(date)
                .description(description)
                .category(category)
                .user(user)
                .build();
    }

    private Category findCategory(List<Category> categories, String name) {
        return categories.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Default category not found: " + name));
    }
}

