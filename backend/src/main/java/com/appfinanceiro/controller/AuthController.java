package com.appfinanceiro.controller;

import com.appfinanceiro.dto.request.*;
import com.appfinanceiro.dto.response.AuthResponseDTO;
import com.appfinanceiro.dto.response.UserResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação & Usuário", description = "Endpoints de cadastro, login, refresh token e gestão de perfil")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Cadastrar novo usuário", description = "Cria uma conta de usuário e inicializa o motor de gamificação (XP, Nível 1)")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida e-mail/senha e retorna os tokens JWT (access + refresh)")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token de acesso", description = "Gera um novo par de tokens usando o refresh token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário autenticado", description = "Retorna os dados do perfil do usuário logado")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponseDTO response = authService.me(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @Operation(summary = "Atualizar perfil", description = "Altera nome ou e-mail do usuário autenticado")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        UserResponseDTO response = authService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    @Operation(summary = "Alterar senha", description = "Valida a senha atual e define uma nova senha")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        authService.changePassword(userPrincipal.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
