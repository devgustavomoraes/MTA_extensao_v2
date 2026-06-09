package com.projetomta.service;

import com.projetomta.domain.entity.Aviso;
import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.PrioridadeAviso;
import com.projetomta.dto.AvisoRequest;
import com.projetomta.dto.AvisoResponse;
import com.projetomta.exception.RecursoNaoEncontradoException;
import com.projetomta.repository.AvisoRepository;
import com.projetomta.repository.UsuarioRepository;
import com.projetomta.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<AvisoResponse> listar(Pageable pageable) {
        return avisoRepository.findByAtivoTrue(pageable).map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public AvisoResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    @Transactional
    public AvisoResponse criar(AvisoRequest request) {
        Usuario publicador = usuarioRepository.findById(SecurityUtils.usuarioAutenticado().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        Aviso aviso = Aviso.builder()
                .titulo(request.getTitulo().trim())
                .mensagem(request.getMensagem().trim())
                .prioridade(request.getPrioridade() != null ? request.getPrioridade() : PrioridadeAviso.NORMAL)
                .ativo(request.getAtivo() != null ? request.getAtivo() : true)
                .publicadoPor(publicador)
                .build();

        return paraResponse(avisoRepository.save(aviso));
    }

    @Transactional
    public AvisoResponse atualizar(Long id, AvisoRequest request) {
        Aviso aviso = buscarEntidade(id);

        aviso.setTitulo(request.getTitulo().trim());
        aviso.setMensagem(request.getMensagem().trim());

        if (request.getPrioridade() != null) {
            aviso.setPrioridade(request.getPrioridade());
        }

        if (request.getAtivo() != null) {
            aviso.setAtivo(request.getAtivo());
        }

        return paraResponse(avisoRepository.save(aviso));
    }

    @Transactional
    public void excluir(Long id) {
        Aviso aviso = buscarEntidade(id);
        aviso.setAtivo(false);
        avisoRepository.save(aviso);
    }

    private Aviso buscarEntidade(Long id) {
        return avisoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aviso não encontrado"));
    }

    private AvisoResponse paraResponse(Aviso aviso) {
        return AvisoResponse.builder()
                .id(aviso.getId())
                .titulo(aviso.getTitulo())
                .mensagem(aviso.getMensagem())
                .prioridade(aviso.getPrioridade())
                .ativo(aviso.getAtivo())
                .publicadoPorId(aviso.getPublicadoPor() != null ? aviso.getPublicadoPor().getId() : null)
                .criadoEm(aviso.getCriadoEm())
                .atualizadoEm(aviso.getAtualizadoEm())
                .build();
    }
}
