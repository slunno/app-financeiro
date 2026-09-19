package com.appfinanceiro.service;

import com.appfinanceiro.domain.User;
import com.appfinanceiro.dto.request.RegisterRequestDTO;
import com.appfinanceiro.dto.response.AuthResponseDTO;
import com.appfinanceiro.exception.EmailAlreadyExistsException;
import com.appfinanceiro.repository.UserRepository;
import com.appfinanceiro.repository.UserXPRepository;
import com.appfinanceiro.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new RegisterRequestDTO("Nathan Henrique", "nathan@exemplo.com", "senha123");
    }

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso e gerar tokens JWT")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(registerDTO.password())).thenReturn("hashed_password");
        
        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .name(registerDTO.name())
                .email(registerDTO.email())
                .password("hashed_password")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtTokenProvider.generateAccessToken(any(), any())).thenReturn("mock_access_token");
        when(jwtTokenProvider.generateRefreshToken(any(), any())).thenReturn("mock_refresh_token");

        AuthResponseDTO response = authService.register(registerDTO);

        assertNotNull(response);
        assertEquals("mock_access_token", response.accessToken());
        assertEquals("mock_refresh_token", response.refreshToken());
        assertEquals("Nathan Henrique", response.user().name());
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistsException ao tentar cadastrar e-mail duplicado")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail(registerDTO.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerDTO));
        verify(userRepository, never()).save(any());
    }
}
