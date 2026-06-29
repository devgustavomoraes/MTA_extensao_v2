package com.projetomta.controller;

import com.projetomta.dto.LoginRequest;
import com.projetomta.dto.LoginResponse;
import com.projetomta.dto.RecuperarSenhaRequest;
import com.projetomta.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/recuperar-senha")
    public ResponseEntity<Map<String, String>> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        authService.solicitarRecuperacaoSenha(request);
        return ResponseEntity.ok(Map.of(
                "mensagem", "Se o e-mail estiver cadastrado, você receberá instruções de recuperação em breve."
        ));
    }

}
