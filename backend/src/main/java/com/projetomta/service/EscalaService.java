package com.projetomta.service;

import com.projetomta.domain.entity.Escala;
import com.projetomta.domain.entity.Evento;
import com.projetomta.domain.entity.Membro;
import com.projetomta.dto.EscalaRequest;
import com.projetomta.dto.EscalaResponse;
import com.projetomta.exception.ConflitoHorarioException;
import com.projetomta.exception.RecursoNaoEncontradoException;
import com.projetomta.exception.RegraNegocioException;
import com.projetomta.repository.EscalaRepository;
import com.projetomta.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EscalaService {

    private final EscalaRepository escalaRepository;
    private final EventoService eventoService;
    private final MembroRepository membroRepository;

    @Transactional(readOnly = true)
    public List<EscalaResponse> listarPorEvento(Long eventoId) {
        eventoService.buscarPorId(eventoId);
        return escalaRepository.findByEventoId(eventoId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public EscalaResponse associarMembro(Long eventoId, EscalaRequest request) {
        Evento evento = eventoService.buscarEntidade(eventoId);
        Membro membro = buscarMembroAtivo(request.getMembroId());

        if (escalaRepository.existsByEventoIdAndMembroId(eventoId, membro.getId())) {
            throw new RegraNegocioException("Este membro já está escalado para este evento");
        }

        validarConflitoMembro(membro.getId(), evento, null);

        Escala escala = Escala.builder()
                .evento(evento)
                .membro(membro)
                .funcaoEscala(request.getFuncaoEscala())
                .confirmado(request.getConfirmado() != null ? request.getConfirmado() : false)
                .build();

        return paraResponse(escalaRepository.save(escala));
    }

    @Transactional
    public EscalaResponse atualizar(Long eventoId, Long escalaId, EscalaRequest request) {
        Escala escala = buscarEscala(eventoId, escalaId);

        if (request.getMembroId() != null && !request.getMembroId().equals(escala.getMembro().getId())) {
            Membro novoMembro = buscarMembroAtivo(request.getMembroId());
            validarConflitoMembro(novoMembro.getId(), escala.getEvento(), eventoId);
            escala.setMembro(novoMembro);
        }

        if (request.getFuncaoEscala() != null) {
            escala.setFuncaoEscala(request.getFuncaoEscala());
        }

        if (request.getConfirmado() != null) {
            escala.setConfirmado(request.getConfirmado());
        }

        return paraResponse(escalaRepository.save(escala));
    }

    @Transactional
    public void remover(Long eventoId, Long escalaId) {
        Escala escala = buscarEscala(eventoId, escalaId);
        escalaRepository.delete(escala);
    }

    private Escala buscarEscala(Long eventoId, Long escalaId) {
        return escalaRepository.findByIdAndEventoId(escalaId, eventoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escala não encontrada"));
    }

    private Membro buscarMembroAtivo(Long membroId) {
        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado"));

        if (!Boolean.TRUE.equals(membro.getAtivo())) {
            throw new RegraNegocioException("Não é possível escalar um membro inativo");
        }

        return membro;
    }

    private void validarConflitoMembro(Long membroId, Evento evento, Long excludeEventoId) {
        if (escalaRepository.existsConflitoMembro(
                membroId,
                evento.getDataInicio(),
                evento.getDataFim(),
                excludeEventoId != null ? excludeEventoId : evento.getId()
        )) {
            throw new ConflitoHorarioException(
                    "Este membro já possui outro evento no mesmo horário"
            );
        }
    }

    private EscalaResponse paraResponse(Escala escala) {
        return EscalaResponse.builder()
                .id(escala.getId())
                .eventoId(escala.getEvento().getId())
                .membroId(escala.getMembro().getId())
                .membroNome(escala.getMembro().getNomeCompleto())
                .funcaoEscala(escala.getFuncaoEscala())
                .confirmado(escala.getConfirmado())
                .criadoEm(escala.getCriadoEm())
                .build();
    }
}
