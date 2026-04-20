package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import com.felicash.auth.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private static final List<String> DEFAULT_INCOME_CATEGORIES = Arrays.asList("Salary", "Transfer");
    private static final List<String> DEFAULT_EXPENSE_CATEGORIES = Arrays.asList("Health", "Food", "Education");

    public CategoryServiceImpl(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllCategoriesByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return categoryRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getCategoriesByUserAndType(String userEmail, TransactionCategoryType type) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return categoryRepository.findByUserAndType(user, type);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id, String userEmail) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (!category.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("You do not have permission to access this category");
        }

        return category;
    }

    @Override
    public void initializeDefaultCategories(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if user already has categories
        List<Category> existingCategories = categoryRepository.findByUser(user);
        if (!existingCategories.isEmpty()) {
            return; // User already has categories, don't create defaults
        }

        // Create default income categories
        for (String categoryName : DEFAULT_INCOME_CATEGORIES) {
            Category category = Category.builder()
                    .name(categoryName)
                    .type(TransactionCategoryType.INCOME)
                    .user(user)
                    .build();
            categoryRepository.save(category);
        }

        // Create default expense categories
        for (String categoryName : DEFAULT_EXPENSE_CATEGORIES) {
            Category category = Category.builder()
                    .name(categoryName)
                    .type(TransactionCategoryType.EXPENSE)
                    .user(user)
                    .build();
            categoryRepository.save(category);
        }
    }
}
