package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Transaction;
import com.appfinanceiro.domain.enums.TransactionStatus;
import com.appfinanceiro.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByUserIdOrderByDateDesc(UUID userId);

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByTransferGroupId(UUID transferGroupId);

    // Saldo derivado: soma por tipo e status para uma conta específica
    @Query("SELECT COALESCE(SUM(CASE " +
           "WHEN t.type = 'INCOME' THEN t.amount " +
           "WHEN t.type = 'EXPENSE' THEN -t.amount " +
           "WHEN t.type = 'TRANSFER' AND t.transferDirection = 'IN' THEN t.amount " +
           "WHEN t.type = 'TRANSFER' AND t.transferDirection = 'OUT' THEN -t.amount " +
           "ELSE 0 END), 0) " +
           "FROM Transaction t WHERE t.account.id = :accountId AND t.status = :status")
    BigDecimal calculateNetAmountByAccountIdAndStatus(
            @Param("accountId") UUID accountId,
            @Param("status") TransactionStatus status
    );

    // Receitas/despesas do período (exclui transferências)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "AND t.transferGroupId IS NULL")
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Despesas por categoria (exclui transferências)
    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "AND t.transferGroupId IS NULL " +
           "GROUP BY t.category.name")
    List<Object[]> sumExpensesByCategoryAndPeriod(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId")
    long countByUserId(@Param("userId") UUID userId);

    // Verifica se conta tem transações (para decidir arquivar vs deletar)
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.account.id = :accountId")
    boolean existsByAccountId(@Param("accountId") UUID accountId);

    // Busca todas as parcelas vinculadas a um Installment via credit_card_invoice
    @Query("SELECT t FROM Transaction t WHERE t.creditCard.id = :cardId AND t.description LIKE :descPrefix " +
           "AND t.creditCardInvoice IS NOT NULL ORDER BY t.date")
    List<Transaction> findInstallmentTransactions(
            @Param("cardId") UUID cardId,
            @Param("descPrefix") String descriptionPrefix
    );
}
