package com.appfinanceiro.controller;

import com.appfinanceiro.domain.enums.TransactionType;
import com.appfinanceiro.dto.request.CategoryRequestDTO;
import com.appfinanceiro.dto.response.CategoryResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categorias Financeiras", description = "Endpoints de listagem e cadastro de categorias de receitas/despesas")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Listar categorias disponíveis (padrão do sistema + customizadas)")
    public ResponseEntity<List<CategoryResponseDTO>> listAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) TransactionType type) {
        return ResponseEntity.ok(categoryService.findAllForUser(userPrincipal.getId(), type));
    }

    @PostMapping
    @Operation(summary = "Criar categoria customizada")
    public ResponseEntity<CategoryResponseDTO> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCustomCategory(userPrincipal.getId(), request));
    }
}
