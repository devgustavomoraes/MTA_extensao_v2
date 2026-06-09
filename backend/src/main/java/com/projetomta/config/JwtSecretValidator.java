package com.projetomta.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSecretValidator {

    private final AppProperties appProperties;

    @PostConstruct
    void validarSegredoJwt() {
        String secret = appProperties.getJwt().getSecret();

        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET inválido: deve ter pelo menos 32 bytes. Configure a variável de ambiente."
            );
        }

        if (secret.toLowerCase().contains("changeme")) {
            log.warn("JWT_SECRET padrão de desenvolvimento detectado. Altere antes de ir para produção.");
        }
    }
}
