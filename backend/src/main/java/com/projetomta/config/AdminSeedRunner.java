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
        var admins = appProperties.getSeed().getAdmins();
        if (admins == null || admins.isEmpty()) {
            log.warn("Nenhuma conta admin em application-admins.yml — copie application-admins.example.yml");
            return;
        }

        int criados = 0;
        for (AppProperties.AdminAccount conta : admins) {
            if (conta.getEmail() == null || conta.getEmail().isBlank()) {
                continue;
            }
            boolean existia = authService.existeUsuario(conta.getEmail());
            authService.criarUsuarioAdminSeNecessario(conta.getEmail(), conta.getPassword());
            if (!existia) {
                criados++;
            }
        }

        log.info("Administradores verificados: {} conta(s) configurada(s), {} nova(s) criada(s)",
                admins.size(), criados);
    }
}
