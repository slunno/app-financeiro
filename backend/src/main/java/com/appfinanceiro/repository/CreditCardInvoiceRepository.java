package com.appfinanceiro.repository;

import com.appfinanceiro.domain.CreditCardInvoice;
import com.appfinanceiro.domain.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardInvoiceRepository extends JpaRepository<CreditCardInvoice, UUID> {
    List<CreditCardInvoice> findByCreditCardId(UUID creditCardId);

    Optional<CreditCardInvoice> findByCreditCardIdAndReferenceMonthAndReferenceYear(
            UUID creditCardId, Integer referenceMonth, Integer referenceYear
    );

    @Query("SELECT i FROM CreditCardInvoice i WHERE i.creditCard.user.id = :userId AND i.status = :status")
    List<CreditCardInvoice> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") InvoiceStatus status);

    @Query("SELECT i FROM CreditCardInvoice i WHERE i.id = :id AND i.creditCard.user.id = :userId")
    Optional<CreditCardInvoice> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
