package com.projetomta.controller;

import com.projetomta.dto.EscalaRequest;
import com.projetomta.dto.EscalaResponse;
import com.projetomta.service.EscalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{eventoId}/escalas")
@RequiredArgsConstructor
public class EscalaController {

    private final EscalaService escalaService;

    @GetMapping
    public ResponseEntity<List<EscalaResponse>> listar(@PathVariable Long eventoId) {
        return ResponseEntity.ok(escalaService.listarPorEvento(eventoId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EscalaResponse> associar(
            @PathVariable Long eventoId,
            @Valid @RequestBody EscalaRequest request
    ) {
        EscalaResponse response = escalaService.associarMembro(eventoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{escalaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EscalaResponse> atualizar(
            @PathVariable Long eventoId,
            @PathVariable Long escalaId,
            @Valid @RequestBody EscalaRequest request
    ) {
        return ResponseEntity.ok(escalaService.atualizar(eventoId, escalaId, request));
    }

    @DeleteMapping("/{escalaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> remover(
            @PathVariable Long eventoId,
            @PathVariable Long escalaId
    ) {
        escalaService.remover(eventoId, escalaId);
        return ResponseEntity.noContent().build();
    }
}
