package com.appfinanceiro.config;

import com.appfinanceiro.domain.User;
import com.appfinanceiro.domain.UserXP;
import com.appfinanceiro.domain.enums.UserRole;
import com.appfinanceiro.repository.UserRepository;
import com.appfinanceiro.repository.UserXPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDefaultUser(
            UserRepository userRepository,
            UserXPRepository userXPRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            String email = "nathanhlima10@gmail.com";
            if (!userRepository.existsByEmail(email)) {
                log.info("Criando usuário inicial: {}", email);
                
                User user = User.builder()
                        .name("nathan")
                        .email(email)
                        .password(passwordEncoder.encode("Nh84480214@"))
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

                log.info("Usuário 'nathan' ({}) criado com sucesso!", email);
            } else {
                log.info("Usuário '{}' já existe no banco de dados.", email);
            }
        };
    }
}
