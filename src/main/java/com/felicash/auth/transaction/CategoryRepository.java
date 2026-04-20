package com.felicash.auth.transaction;

import com.felicash.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByUser(User user);

    List<Category> findByUserAndType(User user, TransactionCategoryType type);

    Optional<Category> findByUserAndName(User user, String name);
}
