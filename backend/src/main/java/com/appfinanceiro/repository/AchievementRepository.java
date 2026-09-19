package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {
    Optional<Achievement> findByCode(String code);
}
