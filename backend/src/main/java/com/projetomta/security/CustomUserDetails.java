package com.projetomta.security;

import com.projetomta.domain.entity.Usuario;
import com.projetomta.domain.enums.Perfil;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String senhaHash;
    private final Perfil perfil;
    private final boolean ativo;
    private final LocalDateTime bloqueadoAte; // 🔒 ADICIONADO

    public CustomUserDetails(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.senhaHash = usuario.getSenhaHash();
        this.perfil = usuario.getPerfil();
        this.ativo = Boolean.TRUE.equals(usuario.getAtivo());
        this.bloqueadoAte = usuario.getBloqueadoAte(); // 🔒 ADICIONADO
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 🔒 CORRIGIDO: Spring Security agora reconhece o bloqueio corretamente
    @Override
    public boolean isAccountNonLocked() {
        return bloqueadoAte == null || !bloqueadoAte.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
