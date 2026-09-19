package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Goal;
import com.appfinanceiro.domain.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {
    List<Goal> findByUserId(UUID userId);
    List<Goal> findByUserIdAndStatus(UUID userId, GoalStatus status);
    Optional<Goal> findByIdAndUserId(UUID id, UUID userId);
    long countByUserIdAndStatus(UUID userId, GoalStatus status);
}
