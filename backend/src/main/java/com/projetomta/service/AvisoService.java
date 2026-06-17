package com.projetomta.service;

import com.projetomta.domain.entity.Aviso;
import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.PrioridadeAviso;
import com.projetomta.dto.AvisoRequest;
import com.projetomta.dto.AvisoResponse;
import com.projetomta.exception.RecursoNaoEncontradoException;
import com.projetomta.exception.RegraNegocioException;
import com.projetomta.repository.AvisoRepository;
import com.projetomta.repository.UsuarioRepository;
import com.projetomta.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AvisoService {

    private final AvisoRepository avisoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Page<AvisoResponse> listar(boolean incluirExpirados, Pageable pageable) {
        LocalDateTime agora = LocalDateTime.now();
        boolean adminComHistorico = incluirExpirados && SecurityUtils.isAdminAutenticado();

        Page<Aviso> pagina = adminComHistorico
                ? avisoRepository.findByAtivoTrue(pageable)
                : avisoRepository.findAtivosVigentes(agora, pageable);

        return pagina.map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public AvisoResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    @Transactional
    public AvisoResponse criar(AvisoRequest request) {
        validarDataExpiracao(request.getDataExpiracao());

        Usuario publicador = usuarioRepository.findById(SecurityUtils.usuarioAutenticado().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado"));

        Aviso aviso = Aviso.builder()
                .titulo(request.getTitulo().trim())
                .mensagem(request.getMensagem().trim())
                .prioridade(request.getPrioridade() != null ? request.getPrioridade() : PrioridadeAviso.NORMAL)
                .ativo(request.getAtivo() != null ? request.getAtivo() : true)
                .dataExpiracao(request.getDataExpiracao())
                .publicadoPor(publicador)
                .build();

        return paraResponse(avisoRepository.save(aviso));
    }

    @Transactional
    public AvisoResponse atualizar(Long id, AvisoRequest request) {
        Aviso aviso = buscarEntidade(id);

        if (request.getDataExpiracao() != null) {
            validarDataExpiracao(request.getDataExpiracao());
            aviso.setDataExpiracao(request.getDataExpiracao());
        }

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

    private void validarDataExpiracao(LocalDateTime dataExpiracao) {
        if (dataExpiracao == null) {
            throw new RegraNegocioException("Informe a data e hora de término do comunicado");
        }
        if (!dataExpiracao.isAfter(LocalDateTime.now())) {
            throw new RegraNegocioException("A data de término deve ser no futuro");
        }
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
                .dataExpiracao(aviso.getDataExpiracao())
                .build();
    }
}
