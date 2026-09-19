package com.appfinanceiro.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDTO(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp,
    List<String> fieldErrors
) {
    public ErrorResponseDTO(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }

    public ErrorResponseDTO(int status, String error, String message, String path, List<String> fieldErrors) {
        this(status, error, message, path, LocalDateTime.now(), fieldErrors);
    }
}
