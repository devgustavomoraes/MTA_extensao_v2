package com.projetomta.exception;

import com.projetomta.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MSG_SERVICO_INDISPONIVEL =
            "O serviço está temporariamente indisponível. Aguarde alguns segundos e tente novamente.";

    @ExceptionHandler({
            CannotGetJdbcConnectionException.class,
            DataAccessResourceFailureException.class,
            QueryTimeoutException.class,
            CannotCreateTransactionException.class,
            SQLTransientConnectionException.class,
            SQLNonTransientConnectionException.class
    })
    public ResponseEntity<ErrorResponse> handleFalhaBancoDados(Exception ex) {
        log.error("Falha de conexão ou consulta ao banco de dados: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .erro("SERVICO_INDISPONIVEL")
                .mensagem(MSG_SERVICO_INDISPONIVEL)
                .build());
    }

    @ExceptionHandler(ContaBloqueadaException.class)
    public ResponseEntity<ErrorResponse> handleContaBloqueada(ContaBloqueadaException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.LOCKED.value())
                .erro("CONTA_BLOQUEADA")
                .mensagem(ex.getMessage() + ". Tente novamente após " + ex.getBloqueadoAte())
                .build());
    }

    @ExceptionHandler(ConflitoHorarioException.class)
    public ResponseEntity<ErrorResponse> handleConflitoHorario(ConflitoHorarioException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .erro("CONFLITO_HORARIO")
                .mensagem(ex.getMessage())
                .build());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraNegocio(RegraNegocioException ex) {
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .erro("REGRA_NEGOCIO")
                .mensagem(ex.getMessage())
                .build());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .erro("NAO_ENCONTRADO")
                .mensagem(ex.getMessage())
                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAcessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .erro("ACESSO_NEGADO")
                .mensagem("Você não tem permissão para realizar esta ação.")
                .build());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .erro("CREDENCIAIS_INVALIDAS")
                .mensagem(ex.getMessage())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .erro("VALIDACAO")
                .mensagem(mensagem)
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex) {
        log.error("Erro não tratado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .erro("ERRO_INTERNO")
                .mensagem("Ocorreu um erro inesperado. Tente novamente mais tarde.")
                .build());
    }
}
