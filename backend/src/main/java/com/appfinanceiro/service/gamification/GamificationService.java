package com.appfinanceiro.service.gamification;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.NotificationType;
import com.appfinanceiro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final UserXPRepository userXPRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final MissionRepository missionRepository;
    private final NotificationRepository notificationRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public UserXP addXp(UUID userId, long amount, String reason) {
        UserXP userXP = userXPRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserXP não encontrado para o usuário"));

        long newCurrentXp = userXP.getCurrentXp() + amount;
        long newTotalXp = userXP.getTotalXp() + amount;
        userXP.setTotalXp(newTotalXp);

        int currentLevel = userXP.getCurrentLevel();
        long xpNeededForNextLevel = currentLevel * 200L;

        boolean levelUp = false;
        while (newCurrentXp >= xpNeededForNextLevel) {
            newCurrentXp -= xpNeededForNextLevel;
            currentLevel++;
            xpNeededForNextLevel = currentLevel * 200L;
            levelUp = true;
        }

        userXP.setCurrentXp(newCurrentXp);
        userXP.setCurrentLevel(currentLevel);
        userXP.setLastActivityDate(LocalDate.now());

        userXP = userXPRepository.save(userXP);

        if (levelUp) {
            notificationRepository.save(Notification.builder()
                    .user(userXP.getUser())
                    .title("Subiu de Nível! 🎉")
                    .message("Parabéns! Você alcançou o Nível " + currentLevel + "! Sua disciplina financeira está pagando dividendos.")
                    .type(NotificationType.LEVEL_UP)
                    .build());
        }

        checkAndUnlockAchievements(userXP.getUser(), userXP);

        return userXP;
    }

    @Transactional
    public void processTransactionEvent(User user) {
        // Concede +10 XP por registrar lançamento financeiro
        addXp(user.getId(), 10L, "Registro de Transação");

        // Atualiza missões ativas
        List<Mission> activeMissions = missionRepository.findByUserId(user.getId());
        for (Mission mission : activeMissions) {
            if (mission.getCurrentCount() < mission.getTargetCount()) {
                mission.setCurrentCount(mission.getCurrentCount() + 1);
                if (mission.getCurrentCount() >= mission.getTargetCount()) {
                    mission.setStatus(com.appfinanceiro.domain.enums.MissionStatus.COMPLETED);
                    addXp(user.getId(), mission.getXpReward(), "Missão Concluída: " + mission.getTitle());
                    notificationRepository.save(Notification.builder()
                            .user(user)
                            .title("Missão Concluída! 🎯")
                            .message("Você completou a missão '" + mission.getTitle() + "' e ganhou +" + mission.getXpReward() + " XP!")
                            .type(NotificationType.MISSION_COMPLETED)
                            .build());
                }
                missionRepository.save(mission);
            }
        }
    }

    private void checkAndUnlockAchievements(User user, UserXP userXP) {
        List<Achievement> allAchievements = achievementRepository.findAll();

        for (Achievement achievement : allAchievements) {
            boolean alreadyUnlocked = userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId());
            if (alreadyUnlocked) continue;

            boolean conditionMet = false;
            switch (achievement.getConditionType()) {
                case "TRANSACTION_COUNT":
                    long count = transactionRepository.countByUserId(user.getId());
                    if (count >= achievement.getConditionThreshold()) conditionMet = true;
                    break;
                case "STREAK_DAYS":
                    if (userXP.getStreakDays() >= achievement.getConditionThreshold()) conditionMet = true;
                    break;
                default:
                    break;
            }

            if (conditionMet) {
                userAchievementRepository.save(UserAchievement.builder()
                        .user(user)
                        .achievement(achievement)
                        .build());

                addXp(user.getId(), achievement.getXpReward(), "Conquista Desbloqueada: " + achievement.getTitle());

                notificationRepository.save(Notification.builder()
                        .user(user)
                        .title("Nova Conquista Desbloqueada! 🏆")
                        .message("Você conquistou '" + achievement.getTitle() + "': " + achievement.getDescription())
                        .type(NotificationType.ACHIEVEMENT_UNLOCKED)
                        .build());
            }
        }
    }
}
