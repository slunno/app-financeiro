package com.appfinanceiro.repository;

import com.appfinanceiro.domain.Mission;
import com.appfinanceiro.domain.enums.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {
    List<Mission> findByUserId(UUID userId);
    List<Mission> findByUserIdAndStatus(UUID userId, MissionStatus status);
    Optional<Mission> findByIdAndUserId(UUID id, UUID userId);
}
