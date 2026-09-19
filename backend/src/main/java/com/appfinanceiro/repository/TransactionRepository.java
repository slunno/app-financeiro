package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Transaction;
import com.appfinanceiro.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserIdOrderByDateDesc(UUID userId);

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate GROUP BY t.category.name")
    List<Object[]> sumExpensesByCategoryAndPeriod(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);
}
