package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserId(UUID userId);
    List<Account> findByUserIdAndArchivedAtIsNull(UUID userId);
    Optional<Account> findByIdAndUserId(UUID id, UUID userId);
    Optional<Account> findByIdAndUserIdAndArchivedAtIsNull(UUID id, UUID userId);
}
