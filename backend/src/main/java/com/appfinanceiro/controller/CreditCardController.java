package com.appfinanceiro.controller;

import com.appfinanceiro.dto.request.CreditCardRequestDTO;
import com.appfinanceiro.dto.response.CreditCardInvoiceResponseDTO;
import com.appfinanceiro.dto.response.CreditCardResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.CreditCardService;
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
@RequestMapping("/credit-cards")
@RequiredArgsConstructor
@Tag(name = "Cartões de Crédito", description = "Endpoints de CRUD de cartões, acompanhamento de faturas e pagamentos")
public class CreditCardController {

    private final CreditCardService creditCardService;

    @GetMapping
    @Operation(summary = "Listar cartões de crédito do usuário")
    public ResponseEntity<List<CreditCardResponseDTO>> listAll(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(creditCardService.findAllByUser(userPrincipal.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cartão por ID")
    public ResponseEntity<CreditCardResponseDTO> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(creditCardService.findByIdAndUser(id, userPrincipal.getId()));
    }

    @PostMapping
    @Operation(summary = "Cadastrar cartão de crédito")
    public ResponseEntity<CreditCardResponseDTO> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreditCardRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(creditCardService.create(userPrincipal.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cartão de crédito")
    public ResponseEntity<CreditCardResponseDTO> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody CreditCardRequestDTO request) {
        return ResponseEntity.ok(creditCardService.update(id, userPrincipal.getId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cartão de crédito")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        creditCardService.delete(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cardId}/invoices")
    @Operation(summary = "Listar faturas do cartão")
    public ResponseEntity<List<CreditCardInvoiceResponseDTO>> getInvoices(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID cardId) {
        return ResponseEntity.ok(creditCardService.findInvoicesByCard(cardId, userPrincipal.getId()));
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    @Operation(summary = "Pagar fatura do cartão")
    public ResponseEntity<CreditCardInvoiceResponseDTO> payInvoice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(creditCardService.payInvoice(invoiceId, userPrincipal.getId()));
    }
}
