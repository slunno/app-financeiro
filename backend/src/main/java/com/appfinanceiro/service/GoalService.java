package com.appfinanceiro.service;

import com.appfinanceiro.domain.*;
import com.appfinanceiro.domain.enums.GoalStatus;
import com.appfinanceiro.domain.enums.NotificationType;
import com.appfinanceiro.dto.request.GoalContributionRequestDTO;
import com.appfinanceiro.dto.request.GoalRequestDTO;
import com.appfinanceiro.dto.response.GoalResponseDTO;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.GoalContributionRepository;
import com.appfinanceiro.repository.GoalRepository;
import com.appfinanceiro.repository.NotificationRepository;
import com.appfinanceiro.repository.UserRepository;
import com.appfinanceiro.service.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final GoalContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final GamificationService gamificationService;

    @Transactional(readOnly = true)
    public List<GoalResponseDTO> findAllByUser(UUID userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoalResponseDTO findByIdAndUser(UUID id, UUID userId) {
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira", "id", id));
        return mapToResponse(goal);
    }

    @Transactional
    public GoalResponseDTO create(UUID userId, GoalRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        BigDecimal initialAmount = request.currentAmount() != null ? request.currentAmount() : BigDecimal.ZERO;
        GoalStatus status = initialAmount.compareTo(request.targetAmount()) >= 0 ? GoalStatus.COMPLETED : GoalStatus.IN_PROGRESS;

        Goal goal = Goal.builder()
                .user(user)
                .title(request.title())
                .description(request.description())
                .targetAmount(request.targetAmount())
                .currentAmount(initialAmount)
                .targetDate(request.targetDate())
                .categoryIcon(request.categoryIcon() != null ? request.categoryIcon() : "Target")
                .status(status)
                .build();

        goal = goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Transactional
    public GoalResponseDTO addContribution(UUID goalId, UUID userId, GoalContributionRequestDTO request) {
        Goal goal = goalRepository.findByIdAndUserId(goalId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira", "id", goalId));

        BigDecimal newAmount = goal.getCurrentAmount().add(request.amount());
        goal.setCurrentAmount(newAmount);

        boolean justCompleted = false;
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0 && goal.getStatus() != GoalStatus.COMPLETED) {
            goal.setStatus(GoalStatus.COMPLETED);
            justCompleted = true;
        }

        goal = goalRepository.save(goal);

        GoalContribution contribution = GoalContribution.builder()
                .goal(goal)
                .amount(request.amount())
                .contributionDate(request.contributionDate() != null ? request.contributionDate() : LocalDate.now())
                .notes(request.notes())
                .build();
        contributionRepository.save(contribution);

        if (justCompleted) {
            gamificationService.addXp(userId, 200L, "Meta Financeira Concluída");

            notificationRepository.save(Notification.builder()
                    .user(goal.getUser())
                    .title("Meta Conquistada! 🎯🎉")
                    .message("Parabéns! Você alcançou 100% da sua meta '" + goal.getTitle() + "' e conquistou +200 XP!")
                    .type(NotificationType.GOAL_PROGRESS)
                    .build());
        }

        return mapToResponse(goal);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta financeira", "id", id));
        goalRepository.delete(goal);
    }

    public GoalResponseDTO mapToResponse(Goal goal) {
        double progress = 0.0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = goal.getCurrentAmount().divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
            if (progress > 100.0) progress = 100.0;
        }

        return new GoalResponseDTO(
                goal.getId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                progress,
                goal.getTargetDate(),
                goal.getCategoryIcon(),
                goal.getStatus()
        );
    }
}
