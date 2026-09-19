package com.appfinanceiro.config;

import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.UserXP;
import com.appfinanceiro.domain.enums.UserRole;
import com.appfinanceiro.repository.UserRepository;
import com.appfinanceiro.repository.UserXPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@Profile("dev")
public class DemoDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    @Value("${DEMO_USER_PASSWORD:Demo123456!}")
    private String demoPassword;

    @Bean
    public CommandLineRunner initDemoUser(
            UserRepository userRepository,
            UserXPRepository userXPRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "demo@finanzas.local";
            if (!userRepository.existsByEmail(email)) {
                log.info("Criando usuário de demonstração (perfil dev): {}", email);

                User user = User.builder()
                        .name("Usuário Demo")
                        .email(email)
                        .password(passwordEncoder.encode(demoPassword))
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

                userXPRepository.save(userXP);

                log.info("Usuário demo ({}) inicializado no ambiente dev.", email);
            }
        };
    }
}
