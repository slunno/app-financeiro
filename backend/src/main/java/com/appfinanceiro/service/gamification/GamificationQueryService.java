package com.appfinanceiro.service.gamification;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.MissionDifficulty;
import com.appfinanceiro.domain.enums.MissionFrequency;
import com.appfinanceiro.domain.enums.MissionStatus;
import com.appfinanceiro.dto.response.*;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GamificationQueryService {

    private final UserXPRepository userXPRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    @Transactional
    public GamificationSummaryDTO getSummary(UUID userId) {
        UserXP userXP = userXPRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserXP não encontrado"));

        ensureUserHasActiveMissions(userXP.getUser());

        List<Mission> missions = missionRepository.findByUserId(userId);
        List<MissionResponseDTO> missionDTOs = missions.stream().map(this::mapMissionToResponse).toList();

        List<Achievement> allAchievements = achievementRepository.findAll();
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        Map<UUID, UserAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(ua -> ua.getAchievement().getId(), ua -> ua));

        List<AchievementResponseDTO> achievementDTOs = allAchievements.stream().map(a -> {
            boolean unlocked = userAchievementMap.containsKey(a.getId());
            return new AchievementResponseDTO(
                    a.getId(),
                    a.getCode(),
                    a.getTitle(),
                    a.getDescription(),
                    a.getIcon(),
                    a.getCategory(),
                    a.getXpReward(),
                    unlocked,
                    unlocked ? userAchievementMap.get(a.getId()).getUnlockedAt() : null
            );
        }).toList();

        UserXPResponseDTO xpDTO = new UserXPResponseDTO(
                userXP.getId(),
                userXP.getCurrentLevel(),
                userXP.getCurrentXp(),
                userXP.getTotalXp(),
                userXP.getStreakDays(),
                userXP.getLastActivityDate(),
                userXP.getFinancialHealthScore()
        );

        return new GamificationSummaryDTO(
                xpDTO,
                missionDTOs,
                achievementDTOs,
                userAchievements.size(),
                allAchievements.size()
        );
    }

    private void ensureUserHasActiveMissions(User user) {
        List<Mission> active = missionRepository.findByUserIdAndStatus(user.getId(), MissionStatus.ACTIVE);
        if (active.isEmpty()) {
            Mission m1 = Mission.builder()
                    .user(user)
                    .title("Registrador Frequente")
                    .description("Registre 3 transações financeiras nesta semana")
                    .frequency(MissionFrequency.WEEKLY)
                    .difficulty(MissionDifficulty.EASY)
                    .xpReward(100L)
                    .targetCount(3)
                    .currentCount(0)
                    .status(MissionStatus.ACTIVE)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(7))
                    .build();

            Mission m2 = Mission.builder()
                    .user(user)
                    .title("Poupador do Mês")
                    .description("Mantenha seus gastos dentro do orçamento estipulado")
                    .frequency(MissionFrequency.MONTHLY)
                    .difficulty(MissionDifficulty.MEDIUM)
                    .xpReward(250L)
                    .targetCount(1)
                    .currentCount(0)
                    .status(MissionStatus.ACTIVE)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(30))
                    .build();

            missionRepository.save(m1);
            missionRepository.save(m2);
        }
    }

    public MissionResponseDTO mapMissionToResponse(Mission m) {
        double progress = 0.0;
        if (m.getTargetCount() > 0) {
            progress = ((double) m.getCurrentCount() / m.getTargetCount()) * 100.0;
            if (progress > 100.0) progress = 100.0;
        }

        return new MissionResponseDTO(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getFrequency(),
                m.getDifficulty(),
                m.getXpReward(),
                m.getTargetCount(),
                m.getCurrentCount(),
                progress,
                m.getStatus(),
                m.getEndDate()
        );
    }
}
