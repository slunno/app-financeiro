package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Category;
import com.appfinanceiro.domain.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    
    @Query("SELECT c FROM Category c WHERE c.isSystemDefault = true OR c.user.id = :userId")
    List<Category> findAllAvailableForUser(@Param("userId") UUID userId);

    @Query("SELECT c FROM Category c WHERE (c.isSystemDefault = true OR c.user.id = :userId) AND c.type = :type")
    List<Category> findAvailableForUserAndType(@Param("userId") UUID userId, @Param("type") TransactionType type);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.isSystemDefault = true OR c.user.id = :userId)")
    Optional<Category> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
