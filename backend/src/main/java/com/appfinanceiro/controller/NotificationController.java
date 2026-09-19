package com.appfinanceiro.controller;

import com.appfinanceiro.dto.response.NotificationResponseDTO;
import com.appfinanceiro.security.UserPrincipal;
import com.appfinanceiro.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Endpoints para consulta e leitura de alertas de contas, metas e gamificação")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Listar todas as notificações do usuário")
    public ResponseEntity<List<NotificationResponseDTO>> listAll(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.findAllByUser(userPrincipal.getId()));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Obter contagem de notificações não lidas")
    public ResponseEntity<Long> countUnread(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(notificationService.countUnread(userPrincipal.getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar notificação específica como lida")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        notificationService.markAsRead(id, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Marcar todas as notificações como lidas")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        notificationService.markAllAsRead(userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
