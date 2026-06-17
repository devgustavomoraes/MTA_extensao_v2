package com.projetomta.controller;

import com.projetomta.dto.AvisoRequest;
import com.projetomta.dto.AvisoResponse;
import com.projetomta.service.AvisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/avisos")
@RequiredArgsConstructor
public class AvisoController {

    private final AvisoService avisoService;

    @GetMapping
    public ResponseEntity<Page<AvisoResponse>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean incluirExpirados,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(avisoService.listar(incluirExpirados, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvisoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(avisoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AvisoResponse> criar(@Valid @RequestBody AvisoRequest request) {
        AvisoResponse response = avisoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AvisoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AvisoRequest request
    ) {
        return ResponseEntity.ok(avisoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        avisoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
