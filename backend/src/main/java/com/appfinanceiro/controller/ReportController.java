package com.appfinanceiro.controller;

import com.appfinanceiro.dto.response.MonthlyComparisonDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Relatórios Financeiros", description = "Endpoints para análise de fluxo de caixa, evolução de patrimônio e comparativo mensal")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly-comparison")
    @Operation(summary = "Comparativo mensal de Receitas x Despesas")
    public ResponseEntity<List<MonthlyComparisonDTO>> getMonthlyComparison(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(reportService.getMonthlyComparison(userPrincipal.getId(), months));
    }
}
