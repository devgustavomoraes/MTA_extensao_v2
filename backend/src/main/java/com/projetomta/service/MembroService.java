package com.projetomta.service;

import com.projetomta.domain.entity.Membro;
import com.projetomta.dto.MembroRequest;
import com.projetomta.dto.MembroResponse;
import com.projetomta.exception.RecursoNaoEncontradoException;
import com.projetomta.exception.RegraNegocioException;
import com.projetomta.repository.MembroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MembroService {

    private final MembroRepository membroRepository;

    @Transactional(readOnly = true)
    public Page<MembroResponse> listar(String busca, Pageable pageable) {
        Page<Membro> pagina = buscaEmBranco(busca)
                ? membroRepository.findAll(pageable)
                : membroRepository.findByNomeCompletoContainingIgnoreCase(busca.trim(), pageable);

        return pagina.map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public MembroResponse buscarPorId(Long id) {
        return paraResponse(buscarEntidade(id));
    }

    @Transactional
    public MembroResponse criar(MembroRequest request) {
        validarEmailUnico(normalizarEmail(request.getEmail()), null);

        Membro membro = Membro.builder()
                .nomeCompleto(request.getNomeCompleto().trim())
                .email(normalizarEmail(request.getEmail()))
                .telefone(request.getTelefone())
                .funcao(request.getFuncao())
                .observacoes(request.getObservacoes())
                .ativo(request.getAtivo() != null ? request.getAtivo() : true)
                .build();

        return paraResponse(membroRepository.save(membro));
    }

    @Transactional
    public MembroResponse atualizar(Long id, MembroRequest request) {
        Membro membro = buscarEntidade(id);
        String email = normalizarEmail(request.getEmail());
        validarEmailUnico(email, id);

        membro.setNomeCompleto(request.getNomeCompleto().trim());
        membro.setEmail(email);
        membro.setTelefone(request.getTelefone());
        membro.setFuncao(request.getFuncao());
        membro.setObservacoes(request.getObservacoes());

        if (request.getAtivo() != null) {
            membro.setAtivo(request.getAtivo());
        }

        return paraResponse(membroRepository.save(membro));
    }

    @Transactional
    public void excluir(Long id) {
        Membro membro = buscarEntidade(id);
        membro.setAtivo(false);
        membroRepository.save(membro);
    }

    private Membro buscarEntidade(Long id) {
        return membroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Membro não encontrado"));
    }

    private MembroResponse paraResponse(Membro membro) {
        return MembroResponse.builder()
                .id(membro.getId())
                .nomeCompleto(membro.getNomeCompleto())
                .email(membro.getEmail())
                .telefone(membro.getTelefone())
                .funcao(membro.getFuncao())
                .ativo(membro.getAtivo())
                .observacoes(membro.getObservacoes())
                .criadoEm(membro.getCriadoEm())
                .atualizadoEm(membro.getAtualizadoEm())
                .build();
    }

    private boolean buscaEmBranco(String busca) {
        return busca == null || busca.isBlank();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private void validarEmailUnico(String email, Long idExcluir) {
        if (email == null) {
            return;
        }

        boolean duplicado = idExcluir == null
                ? membroRepository.existsByEmailIgnoreCase(email)
                : membroRepository.existsByEmailIgnoreCaseAndIdNot(email, idExcluir);

        if (duplicado) {
            throw new RegraNegocioException("Já existe um membro cadastrado com este e-mail");
        }
    }
}
