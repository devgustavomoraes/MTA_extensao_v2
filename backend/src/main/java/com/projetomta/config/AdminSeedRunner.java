package com.projetomta.config;

import com.projetomta.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeedRunner implements ApplicationRunner {

    private final AuthService authService;
    private final AppProperties appProperties;

    @Override
    public void run(ApplicationArguments args) {
        String email = appProperties.getSeed().getAdminEmail();
        String senha = appProperties.getSeed().getAdminPassword();

        if (email == null || email.isBlank()) {
            return;
        }

        authService.criarUsuarioAdminSeNecessario(email, senha);
        log.info("Verificação de usuário administrador inicial concluída para: {}", email);
    }
}
