package com.appfinanceiro.controller;

import com.appfinanceiro.dto.response.GamificationSummaryDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.gamification.GamificationQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gamification")
@RequiredArgsConstructor
@Tag(name = "Gamificação", description = "Endpoints para consulta do painel de gamificação, nível, XP, conquistas e missões")
public class GamificationController {

    private final GamificationQueryService gamificationQueryService;

    @GetMapping("/summary")
    @Operation(summary = "Obter dados do painel de gamificação", description = "Retorna o nível, XP acumulado, streak, conquistas desbloqueadas e missões ativas")
    public ResponseEntity<GamificationSummaryDTO> getSummary(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(gamificationQueryService.getSummary(userPrincipal.getId()));
    }
}
