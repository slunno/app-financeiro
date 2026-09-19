package com.appfinanceiro.controller;

import com.appfinanceiro.dto.request.BudgetRequestDTO;
import com.appfinanceiro.dto.response.BudgetResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
@Tag(name = "Orçamentos Mensais", description = "Endpoints para acompanhamento e teto de gastos por categoria")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Listar orçamentos do mês/ano")
    public ResponseEntity<List<BudgetResponseDTO>> getBudgets(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        int targetMonth = month != null ? month : LocalDate.now().getMonthValue();
        int targetYear = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(budgetService.findByMonthAndYear(userPrincipal.getId(), targetMonth, targetYear));
    }

    @PostMapping
    @Operation(summary = "Definir ou atualizar orçamento de categoria")
    public ResponseEntity<BudgetResponseDTO> createOrUpdate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BudgetRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createOrUpdate(userPrincipal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover orçamento")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        budgetService.delete(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
