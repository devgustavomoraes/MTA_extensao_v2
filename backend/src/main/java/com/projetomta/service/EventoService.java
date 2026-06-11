package com.projetomta.service;

import com.projetomta.domain.entity.Evento;
import com.projetomta.domain.entity.Usuario;
import com.projetomta.dto.EventoRequest;
import com.projetomta.dto.EventoResponse;
import com.projetomta.exception.ConflitoHorarioException;
import com.projetomta.exception.RecursoNaoEncontradoException;
import com.projetomta.exception.RegraNegocioException;
import com.projetomta.repository.EventoRepository;
import com.projetomta.repository.UsuarioRepository;
import com.projetomta.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<EventoResponse> listar(
            LocalDateTime inicio,
            LocalDateTime fim,
            boolean incluirEncerrados,
            Pageable pageable
    ) {
        LocalDateTime agora = LocalDateTime.now();
        boolean adminComHistorico = incluirEncerrados && SecurityUtils.isAdminAutenticado();

        Page<Evento> pagina;
        if (adminComHistorico) {
            pagina = (inicio != null && fim != null)
                    ? eventoRepository.findByDataInicioBetween(inicio, fim, pageable)
                    : eventoRepository.findAll(pageable);
        } else {
            pagina = eventoRepository.findByDataFimGreaterThanEqualOrderByDataInicioAsc(agora, pageable);
        }

        return pagina.map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public List<EventoResponse> listarProximos() {
        LocalDateTime agora = LocalDateTime.now();
        return eventoRepository
                .findTop10ByDataFimGreaterThanEqualOrderByDataInicioAsc(agora)
                .stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventoResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    @Transactional
    public EventoResponse criar(EventoRequest request) {
        validarPeriodo(request.getDataInicio(), request.getDataFim());
        validarConflitoHorario(request.getDataInicio(), request.getDataFim(), null);

        Usuario criador = usuarioRepository.findById(SecurityUtils.usuarioAutenticado().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        Evento evento = Evento.builder()
                .titulo(request.getTitulo().trim())
                .descricao(request.getDescricao())
                .dataInicio(request.getDataInicio())
                .dataFim(request.getDataFim())
                .local(request.getLocal())
                .criadoPor(criador)
                .build();

        return paraResponse(eventoRepository.save(evento));
    }

    @Transactional
    public EventoResponse atualizar(Long id, EventoRequest request) {
        Evento evento = buscarEntidade(id);

        validarPeriodo(request.getDataInicio(), request.getDataFim());
        validarConflitoHorario(request.getDataInicio(), request.getDataFim(), id);

        evento.setTitulo(request.getTitulo().trim());
        evento.setDescricao(request.getDescricao());
        evento.setDataInicio(request.getDataInicio());
        evento.setDataFim(request.getDataFim());
        evento.setLocal(request.getLocal());

        return paraResponse(eventoRepository.save(evento));
    }

    @Transactional
    public void excluir(Long id) {
        Evento evento = buscarEntidade(id);
        eventoRepository.delete(evento);
    }

    Evento buscarEntidade(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento não encontrado"));
    }

    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (!fim.isAfter(inicio)) {
            throw new RegraNegocioException("A data de fim deve ser posterior à data de início");
        }
    }

    private void validarConflitoHorario(LocalDateTime inicio, LocalDateTime fim, Long excludeId) {
        if (eventoRepository.existsConflitoHorario(inicio, fim, excludeId, LocalDateTime.now())) {
            throw new ConflitoHorarioException(
                    "Já existe um evento agendado neste horário. Escolha outro período."
            );
        }
    }

    private EventoResponse paraResponse(Evento evento) {
        return EventoResponse.builder()
                .id(evento.getId())
                .titulo(evento.getTitulo())
                .descricao(evento.getDescricao())
                .dataInicio(evento.getDataInicio())
                .dataFim(evento.getDataFim())
                .local(evento.getLocal())
                .criadoPorId(evento.getCriadoPor() != null ? evento.getCriadoPor().getId() : null)
                .criadoEm(evento.getCriadoEm())
                .atualizadoEm(evento.getAtualizadoEm())
                .build();
    }
}
