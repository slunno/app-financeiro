package com.appfinanceiro.repository;

import com.appfinanceiro.domain.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, UUID> {
    List<CreditCard> findByUserId(UUID userId);
    Optional<CreditCard> findByIdAndUserId(UUID id, UUID userId);
}
