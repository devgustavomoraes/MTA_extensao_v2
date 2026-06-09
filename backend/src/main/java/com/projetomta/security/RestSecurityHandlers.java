package com.projetomta.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projetomta.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RestSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        escreverErro(response, HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO",
                "Autenticação necessária. Faça login para continuar.");
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {
        escreverErro(response, HttpStatus.FORBIDDEN, "ACESSO_NEGADO",
                "Você não tem permissão para realizar esta ação.");
    }

    private void escreverErro(HttpServletResponse response, HttpStatus status, String erro, String mensagem)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .erro(erro)
                .mensagem(mensagem)
                .build();

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
