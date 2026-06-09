package com.projetomta.security;

import com.projetomta.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppProperties appProperties;

    public String gerarToken(CustomUserDetails userDetails) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + appProperties.getJwt().getExpirationMs());

        return Jwts.builder()
                .subject(userDetails.getEmail())
                .claims(Map.of(
                        "id", userDetails.getId(),
                        "perfil", userDetails.getPerfil().name()
                ))
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chaveSecreta())
                .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public boolean tokenValido(String token, CustomUserDetails userDetails) {
        String email = extrairEmail(token);
        return email.equals(userDetails.getEmail()) && !tokenExpirado(token);
    }

    private boolean tokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chaveSecreta())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey chaveSecreta() {
        byte[] bytes = appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
