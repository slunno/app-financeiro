package com.appfinanceiro.controller;

import com.appfinanceiro.dto.request.GoalContributionRequestDTO;
import com.appfinanceiro.dto.request.GoalRequestDTO;
import com.appfinanceiro.dto.response.GoalResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@Tag(name = "Metas Financeiras", description = "Endpoints para acompanhamento, aporte e progresso de metas")
public class GoalController {

    private final GoalService goalService;

    @GetMapping
    @Operation(summary = "Listar todas as metas do usuário")
    public ResponseEntity<List<GoalResponseDTO>> listAll(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(goalService.findAllByUser(userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta por ID")
    public ResponseEntity<GoalResponseDTO> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(goalService.findByIdAndUser(id, userPrincipal.getId()));
    }

    @PostMapping
    @Operation(summary = "Criar nova meta financeira")
    public ResponseEntity<GoalResponseDTO> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody GoalRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(userPrincipal.getId(), request));
    }

    @PostMapping("/{id}/contribute")
    @Operation(summary = "Adicionar aporte à meta")
    public ResponseEntity<GoalResponseDTO> contribute(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody GoalContributionRequestDTO request) {
        return ResponseEntity.ok(goalService.addContribution(id, userPrincipal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir meta")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        goalService.delete(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
