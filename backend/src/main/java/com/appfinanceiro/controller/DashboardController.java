package com.appfinanceiro.controller;

import com.appfinanceiro.dto.response.DashboardSummaryDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints do painel principal com saldos, gráficos, orçamentos e gamificação")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Obter dados completos do Dashboard")
    public ResponseEntity<DashboardSummaryDTO> getDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(dashboardService.getDashboardSummary(userPrincipal.getId()));
    }
}
