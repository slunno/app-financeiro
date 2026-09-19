package com.appfinanceiro.service;

import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.UserXP;
import com.appfinanceiro.domain.enums.UserRole;
import com.appfinanceiro.dto.request.*;
import com.appfinanceiro.dto.response.AuthResponseDTO;
import com.appfinanceiro.dto.response.UserResponseDTO;
import com.appfinanceiro.dto.response.UserXPResponseDTO;
import com.appfinanceiro.exception.EmailAlreadyExistsException;
import com.appfinanceiro.exception.ResourceNotFoundException;
import com.appfinanceiro.repository.UserRepository;
import com.appfinanceiro.repository.UserXPRepository;
import com.appfinanceiro.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserXPRepository userXPRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

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
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                mapToUserResponse(user),
                mapToUserXPResponse(userXP)
        );
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("E-mail ou senha incorretos.");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", request.email()));

        UserXP userXP = userXPRepository.findByUserId(user.getId())
                .orElseGet(() -> userXPRepository.save(UserXP.builder().user(user).build()));

        // Atualiza streak se aplicável
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
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                mapToUserResponse(user),
                mapToUserXPResponse(userXP)
        );
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        if (!jwtTokenProvider.validateToken(request.refreshToken())) {
            throw new BadCredentialsException("Refresh Token inválido ou expirado.");
        }

        String email = jwtTokenProvider.getEmailFromToken(request.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", email));

        UserXP userXP = userXPRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("UserXP não encontrado"));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        return new AuthResponseDTO(
                newAccessToken,
                newRefreshToken,
                mapToUserResponse(user),
                mapToUserXPResponse(userXP)
        );
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
