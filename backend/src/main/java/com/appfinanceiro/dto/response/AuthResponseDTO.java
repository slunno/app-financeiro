package com.appfinanceiro.dto.response;

public record AuthResponseDTO(
    String accessToken,
    String refreshToken,
    String tokenType,
    UserResponseDTO user,
    UserXPResponseDTO userXp
) {
    public AuthResponseDTO(String accessToken, String refreshToken, UserResponseDTO user, UserXPResponseDTO userXp) {
        this(accessToken, refreshToken, "Bearer", user, userXp);
    }
}
