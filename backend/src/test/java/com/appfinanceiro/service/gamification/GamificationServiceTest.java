package com.appfinanceiro.service.gamification;

import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.UserXP;
import com.appfinanceiro.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private UserXPRepository userXPRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private GamificationService gamificationService;

    private User user;
    private UserXP userXP;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .name("Usuario Teste")
                .email("teste@finanzas.com")
                .build();

        userXP = UserXP.builder()
                .id(UUID.randomUUID())
                .user(user)
                .currentLevel(1)
                .currentXp(50L)
                .totalXp(50L)
                .streakDays(1)
                .financialHealthScore(70)
                .build();
    }

    @Test
    @DisplayName("Deve adicionar XP e manter no mesmo nível quando o limite do nível não for atingido")
    void testAddXpSameLevel() {
        when(userXPRepository.findByUserId(user.getId())).thenReturn(Optional.of(userXP));
        when(userXPRepository.save(any(UserXP.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserXP result = gamificationService.addXp(user.getId(), 50L, "Teste");

        assertEquals(1, result.getCurrentLevel());
        assertEquals(100L, result.getCurrentXp());
        assertEquals(100L, result.getTotalXp());
    }

    @Test
    @DisplayName("Deve subir para o Nível 2 ao atingir 200 XP")
    void testAddXpLevelUp() {
        when(userXPRepository.findByUserId(user.getId())).thenReturn(Optional.of(userXP));
        when(userXPRepository.save(any(UserXP.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Adiciona 150 XP aos 50 XP atuais = 200 XP (Nível 1 requer 200 XP -> sobe pro Nível 2 com 0 XP sobrando)
        UserXP result = gamificationService.addXp(user.getId(), 150L, "Teste Level Up");

        assertEquals(2, result.getCurrentLevel());
        assertEquals(0L, result.getCurrentXp());
        assertEquals(200L, result.getTotalXp());
        verify(notificationRepository, times(1)).save(any());
    }
}
