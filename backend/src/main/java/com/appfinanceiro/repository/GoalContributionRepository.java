package com.appfinanceiro.repository;

import com.appfinanceiro.domain.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalContributionRepository extends JpaRepository<GoalContribution, UUID> {
    List<GoalContribution> findByGoalIdOrderByContributionDateDesc(UUID goalId);
}
