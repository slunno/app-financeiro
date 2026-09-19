package com.appfinanceiro.service;

import com.appfinanceiro.domain.RefreshToken;
import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.UserXP;
import com.appfinanceiro.domain.enums.UserRole;
import com.appfinanceiro.dto.request.*;
import com.appfinanceiro.dto.response.AuthResponseDTO;
import com.appfinanceiro.dto.response.UserResponseDTO;
import com.appfinanceiro.dto.response.UserXPResponseDTO;
import com.appfinanceiro.exception.EmailAlreadyExistsException;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.RefreshTokenRepository;
import com.appfinanceiro.repository.UserRepository;
import com.appfinanceiro.repository.UserXPRepository;
import com.appfinanceiro.security.JwtTokenProvider;
import com.appfinanceiro.security.LoginRateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserXPRepository userXPRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final LoginRateLimiterService loginRateLimiterService;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_USER)
                .build();

        user = userRepository.save(user);

        UserXP userXP = UserXP.builder()
                .user(user)
                .currentLevel(1)
                .currentXp(0L)
                .totalXp(0L)
                .streakDays(1)
                .lastActivityDate(LocalDate.now())
                .financialHealthScore(70)
                .build();

        userXP = userXPRepository.save(userXP);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        createAndSaveRefreshToken(user, refreshTokenStr);

        return new AuthResponseDTO(
                accessToken,
                refreshTokenStr,
                mapToUserResponse(user),
                mapToUserXPResponse(userXP)
        );
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request, String clientIp) {
        loginRateLimiterService.checkRateLimit(clientIp, request.email());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            loginRateLimiterService.recordFailedAttempt(clientIp, request.email());
            throw new BadCredentialsException("E-mail ou senha incorretos.");
        }

        loginRateLimiterService.resetAttempts(clientIp, request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", request.email()));

        UserXP userXP = userXPRepository.findByUserId(user.getId())
                .orElseGet(() -> userXPRepository.save(UserXP.builder().user(user).build()));

        if (userXP.getLastActivityDate() == null || !userXP.getLastActivityDate().equals(LocalDate.now())) {
            if (userXP.getLastActivityDate() != null && userXP.getLastActivityDate().equals(LocalDate.now().minusDays(1))) {
                userXP.setStreakDays(userXP.getStreakDays() + 1);
            } else if (userXP.getLastActivityDate() != null && userXP.getLastActivityDate().isBefore(LocalDate.now().minusDays(1))) {
                userXP.setStreakDays(1);
            }
            userXP.setLastActivityDate(LocalDate.now());
            userXP = userXPRepository.save(userXP);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        createAndSaveRefreshToken(user, refreshTokenStr);

        return new AuthResponseDTO(
                accessToken,
                refreshTokenStr,
                mapToUserResponse(user),
                mapToUserXPResponse(userXP)
        );
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new BadCredentialsException("Refresh Token inválido ou expirado.");
        }

        String tokenHash = hashToken(request.refreshToken());
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Refresh Token não encontrado ou já revogado."));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh Token revogado ou expirado.");
        }

        // Rotação: Revoga o token atual
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        UserXP userXP = userXPRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UserXP não encontrado"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        createAndSaveRefreshToken(user, newRefreshTokenStr);

        return new AuthResponseDTO(
                newAccessToken,
                newRefreshTokenStr,
                mapToUserResponse(user),
                mapToUserXPResponse(userXP)
        );
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            String tokenHash = hashToken(refreshTokenStr);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDTO me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponseDTO updateProfile(UUID userId, UpdateProfileRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        user.setName(request.name());
        user.setEmail(request.email());
        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("A senha atual informada está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private void createAndSaveRefreshToken(User user, String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        RefreshToken refreshToken = new RefreshToken(null, tokenHash, user, expiresAt, false, LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao calcular hash do token", e);
        }
    }

    public UserResponseDTO mapToUserResponse(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }

    public UserXPResponseDTO mapToUserXPResponse(UserXP userXP) {
        return new UserXPResponseDTO(
                userXP.getId(),
                userXP.getCurrentLevel(),
                userXP.getCurrentXp(),
                userXP.getTotalXp(),
                userXP.getStreakDays(),
                userXP.getLastActivityDate(),
                userXP.getFinancialHealthScore()
        );
    }
}
