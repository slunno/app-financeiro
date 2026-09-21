package com.appfinanceiro.controller;

import com.appfinanceiro.dto.request.AccountRequestDTO;
import com.appfinanceiro.dto.request.TransferRequestDTO;
import com.appfinanceiro.dto.response.AccountResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Contas Bancárias", description = "Endpoints de CRUD e transferência entre contas/carteiras")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Listar todas as contas ativas do usuário")
    public ResponseEntity<List<AccountResponseDTO>> listAll(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(accountService.findAllByUser(userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    public ResponseEntity<AccountResponseDTO> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.findByIdAndUser(id, userPrincipal.getId()));
    }

    @PostMapping
    @Operation(summary = "Criar nova conta/carteira")
    public ResponseEntity<AccountResponseDTO> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AccountRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(userPrincipal.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta")
    public ResponseEntity<AccountResponseDTO> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody AccountRequestDTO request) {
        return ResponseEntity.ok(accountService.update(id, userPrincipal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ou arquivar conta")
    public ResponseEntity<?> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        boolean archived = accountService.delete(id, userPrincipal.getId());
        if (archived) {
            return ResponseEntity.ok(Map.of("message", "Conta possui lançamentos vinculados e foi arquivada.", "archived", true));
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transferência entre contas")
    public ResponseEntity<Void> transfer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TransferRequestDTO request) {
        accountService.transfer(userPrincipal.getId(), request);
        return ResponseEntity.ok().build();
    }
}
