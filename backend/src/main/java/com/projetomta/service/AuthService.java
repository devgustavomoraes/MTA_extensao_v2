package com.projetomta.service;

import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.Perfil;
import com.projetomta.dto.LoginRequest;
import com.projetomta.dto.LoginResponse;
import com.projetomta.dto.RecuperarSenhaRequest;
import com.projetomta.exception.ContaBloqueadaException;
import com.projetomta.exception.CredenciaisInvalidasException;
import com.projetomta.repository.UsuarioRepository;
import com.projetomta.security.CustomUserDetails;
import com.projetomta.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptRecorder loginAttemptRecorder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(CredenciaisInvalidasException::new);

        validarContaBloqueada(usuario);

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new CredenciaisInvalidasException();
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            var bloqueadoAte = loginAttemptRecorder.registrarFalha(usuario.getId());
            if (bloqueadoAte.isPresent()) {
                throw new ContaBloqueadaException(bloqueadoAte.get());
            }
            throw new CredenciaisInvalidasException();
        }

        loginAttemptRecorder.registrarSucesso(usuario.getId());

        CustomUserDetails userDetails = new CustomUserDetails(usuario);
        String token = jwtService.gerarToken(userDetails);

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .id(usuario.getId())
                .email(usuario.getEmail())
                .perfil(usuario.getPerfil())
                .build();
    }

    public void solicitarRecuperacaoSenha(RecuperarSenhaRequest request) {
        // Placeholder: integração de e-mail será implementada na Etapa 6 (front-end).
        usuarioRepository.findByEmail(request.getEmail().trim().toLowerCase());
    }

    private void validarContaBloqueada(Usuario usuario) {
        if (usuario.isBloqueado()) {
            throw new ContaBloqueadaException(usuario.getBloqueadoAte());
        }

        loginAttemptRecorder.desbloquearSeExpirado(usuario.getId());
    }

    @Transactional
    public void criarUsuarioAdminSeNecessario(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            return;
        }

        String emailNormalizado = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            return;
        }

        Usuario admin = Usuario.builder()
                .email(emailNormalizado)
                .senhaHash(passwordEncoder.encode(senha))
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .tentativasLogin(0)
                .build();

        usuarioRepository.save(admin);
    }
}
