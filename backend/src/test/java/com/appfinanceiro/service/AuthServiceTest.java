package com.appfinanceiro.service;

import com.appfinanceiro.domain.RefreshToken;
import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.UserXP;
import com.appfinanceiro.dto.request.RefreshTokenRequestDTO;
import com.appfinanceiro.dto.request.RegisterRequestDTO;
import com.appfinanceiro.dto.response.AuthResponseDTO;
import com.appfinanceiro.exception.EmailAlreadyExistsException;
import com.appfinanceiro.repository.RefreshTokenRepository;
import com.appfinanceiro.repository.UserRepository;
import com.appfinanceiro.repository.UserXPRepository;
import com.appfinanceiro.security.JwtTokenProvider;
import com.appfinanceiro.security.LoginRateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserXPRepository userXPRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LoginRateLimiterService loginRateLimiterService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterRequestDTO("Usuário Demo", "demo@finanzas.local", "senha123");
    }

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso e gerar tokens JWT e Refresh Token persistido")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.password())).thenReturn("hashed_password");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .name(registerDTO.name())
                .email(registerDTO.email())
                .password("hashed_password")
                .build();

        UserXP mockUserXP = UserXP.builder().id(UUID.randomUUID()).user(savedUser).currentLevel(1).currentXp(0L).totalXp(0L).streakDays(1).financialHealthScore(70).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userXPRepository.save(any(UserXP.class))).thenReturn(mockUserXP);
        when(jwtTokenProvider.generateAccessToken(any(), any())).thenReturn("mock_access_token");
        when(jwtTokenProvider.generateRefreshToken(any(), any())).thenReturn("mock_refresh_token");

        AuthResponseDTO response = authService.register(registerDTO);

        assertNotNull(response);
        assertEquals("mock_access_token", response.accessToken());
        assertEquals("mock_refresh_token", response.refreshToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistsException ao tentar cadastrar e-mail duplicado")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao tentar renovar token com refresh token revogado")
    void testRefreshTokenRevokedFails() {
        String rawToken = "raw_revoked_token";
        when(jwtTokenProvider.validateToken(rawToken)).thenReturn(true);

        User user = User.builder().id(UUID.randomUUID()).email("demo@finanzas.local").build();
        RefreshToken revokedToken = new RefreshToken(UUID.randomUUID(), "hash", user, LocalDateTime.now().plusDays(1), true, LocalDateTime.now());

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(new RefreshTokenRequestDTO(rawToken)));
    }
}
