package com.projetomta.support;

import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.Perfil;
import com.projetomta.security.CustomUserDetails;
import com.projetomta.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class TestAuthHelper {

    private TestAuthHelper() {
    }

    public static Usuario criarUsuario(String email, String senha, Perfil perfil, PasswordEncoder encoder) {
        return Usuario.builder()
                .email(email)
                .senhaHash(encoder.encode(senha))
                .perfil(perfil)
                .ativo(true)
                .tentativasLogin(0)
                .build();
    }

    public static String gerarToken(Usuario usuario, JwtService jwtService) {
        return jwtService.gerarToken(new CustomUserDetails(usuario));
    }
}
