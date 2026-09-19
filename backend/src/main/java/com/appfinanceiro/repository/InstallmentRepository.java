package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, UUID> {
    List<Installment> findByUserId(UUID userId);
    Optional<Installment> findByIdAndUserId(UUID id, UUID userId);
}
