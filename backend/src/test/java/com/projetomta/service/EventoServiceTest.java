package com.projetomta.service;

import com.projetomta.domain.entity.Evento;
import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.Perfil;
import com.projetomta.dto.EventoRequest;
import com.projetomta.exception.ConflitoHorarioException;
import com.projetomta.exception.RegraNegocioException;
import com.projetomta.repository.EventoRepository;
import com.projetomta.repository.UsuarioRepository;
import com.projetomta.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EventoService eventoService;

    private Usuario admin;

    @BeforeEach
    void setUp() {
        admin = Usuario.builder()
                .id(1L)
                .email("admin@test.com")
                .senhaHash("hash")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(admin);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCriarEventoComPeriodoValido() {
        EventoRequest request = criarRequestValido();

        when(eventoRepository.existsConflitoHorario(any(), any(), isNull(), any())).thenReturn(false);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
            Evento evento = invocation.getArgument(0);
            evento.setId(10L);
            return evento;
        });

        var response = eventoService.criar(request);

        assertEquals("Culto de Domingo", response.getTitulo());
        verify(eventoRepository).save(any(Evento.class));
    }

    @Test
    void deveRejeitarQuandoFimNaoEhPosteriorAoInicio() {
        EventoRequest request = new EventoRequest();
        LocalDateTime inicio = LocalDateTime.of(2026, 6, 10, 14, 0);
        request.setTitulo("Evento Inválido");
        request.setDataInicio(inicio);
        request.setDataFim(inicio.minusHours(1));

        assertThrows(RegraNegocioException.class, () -> eventoService.criar(request));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarQuandoInicioIgualAoFim() {
        EventoRequest request = new EventoRequest();
        LocalDateTime mesmoHorario = LocalDateTime.of(2026, 6, 10, 14, 0);
        request.setTitulo("Evento Inválido");
        request.setDataInicio(mesmoHorario);
        request.setDataFim(mesmoHorario);

        assertThrows(RegraNegocioException.class, () -> eventoService.criar(request));
    }

    @Test
    void deveRejeitarConflitoDeHorario() {
        EventoRequest request = criarRequestValido();

        when(eventoRepository.existsConflitoHorario(any(), any(), isNull(), any())).thenReturn(true);

        assertThrows(ConflitoHorarioException.class, () -> eventoService.criar(request));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void deveExcluirProprioIdAoValidarConflitoNaAtualizacao() {
        EventoRequest request = criarRequestValido();
        Evento existente = Evento.builder()
                .id(5L)
                .titulo("Antigo")
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .build();

        when(eventoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(eventoRepository.existsConflitoHorario(any(), any(), eq(5L), any())).thenReturn(false);
        when(eventoRepository.save(any(Evento.class))).thenReturn(existente);

        eventoService.atualizar(5L, request);

        verify(eventoRepository).existsConflitoHorario(any(), any(), eq(5L), any());
    }

    private EventoRequest criarRequestValido() {
        EventoRequest request = new EventoRequest();
        request.setTitulo("Culto de Domingo");
        request.setDataInicio(LocalDateTime.of(2026, 6, 15, 10, 0));
        request.setDataFim(LocalDateTime.of(2026, 6, 15, 12, 0));
        request.setLocal("Templo Sede");
        return request;
    }
}
