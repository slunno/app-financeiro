package com.appfinanceiro.controller;

import com.appfinanceiro.dto.request.TransactionFilterDTO;
import com.appfinanceiro.dto.request.TransactionRequestDTO;
import com.appfinanceiro.dto.response.TransactionResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações Financeiras", description = "Endpoints de lançamentos de receitas, despesas, parcelamentos e filtros")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Listar transações do usuário com paginação e filtros")
    public ResponseEntity<Page<TransactionResponseDTO>> listAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            TransactionFilterDTO filter,
            @PageableDefault(size = 20, sort = "date") Pageable pageable) {
        return ResponseEntity.ok(transactionService.findAllByUserPaginated(userPrincipal.getId(), filter, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    public ResponseEntity<TransactionResponseDTO> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.findByIdAndUser(id, userPrincipal.getId()));
    }

    @PostMapping
    @Operation(summary = "Registrar nova receita, despesa ou parcelamento")
    public ResponseEntity<List<TransactionResponseDTO>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TransactionRequestDTO request) {
        List<TransactionResponseDTO> response = transactionService.create(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar transação existente")
    public ResponseEntity<TransactionResponseDTO> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequestDTO request) {
        TransactionResponseDTO response = transactionService.update(id, userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir transação")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        transactionService.delete(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
