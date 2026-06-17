package com.projetomta.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseStartupLogger {

    private final DataSource dataSource;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    void validarConexaoBanco() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return;
        }

        String perfil = String.join(",", environment.getActiveProfiles());
        if (perfil.isBlank()) {
            perfil = environment.getProperty("spring.profiles.active", "default");
        }

        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            String jdbcUrl = extrairJdbcUrl();
            log.info("Banco conectado [perfil={}] catalog={} url={}", perfil, catalog, jdbcUrl);
        } catch (Exception ex) {
            log.error("Falha ao validar conexão com o banco [perfil={}]: {}", perfil, ex.getMessage());
            throw new IllegalStateException("Não foi possível conectar ao banco de dados", ex);
        }
    }

    private String extrairJdbcUrl() {
        if (dataSource instanceof HikariDataSource hikari) {
            String url = hikari.getJdbcUrl();
            return url != null ? url.replaceAll("password=[^&]*", "password=***") : "n/a";
        }
        return "datasource";
    }
}
