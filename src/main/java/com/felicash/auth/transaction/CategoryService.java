package com.felicash.auth.transaction;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    List<Category> getAllCategoriesByUser(String userEmail);

    List<Category> getCategoriesByUserAndType(String userEmail, TransactionCategoryType type);

    Category getCategoryById(UUID id, String userEmail);

    void initializeDefaultCategories(String userEmail);
}
