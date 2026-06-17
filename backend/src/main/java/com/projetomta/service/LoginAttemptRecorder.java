package com.projetomta.service;

import com.projetomta.config.AppProperties;
import com.projetomta.domain.entity.Usuario;
import com.projetomta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginAttemptRecorder {

    private final UsuarioRepository usuarioRepository;
    private final AppProperties appProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<LocalDateTime> registrarFalha(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow();

        int tentativas = usuario.getTentativasLogin() + 1;
        usuario.setTentativasLogin(tentativas);

        int maxTentativas = appProperties.getSecurity().getLoginMaxAttempts();
        if (tentativas >= maxTentativas) {
            int duracaoMinutos = appProperties.getSecurity().getLoginLockDurationMin();
            usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(duracaoMinutos));
        }

        usuarioRepository.save(usuario);

        if (usuario.isBloqueado()) {
            return Optional.of(usuario.getBloqueadoAte());
        }

        return Optional.empty();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSucesso(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow();

        usuario.setTentativasLogin(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void desbloquearSeExpirado(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow();

        if (usuario.getBloqueadoAte() != null && !usuario.isBloqueado()) {
            usuario.setBloqueadoAte(null);
            usuario.setTentativasLogin(0);
            usuarioRepository.save(usuario);
        }
    }
}
