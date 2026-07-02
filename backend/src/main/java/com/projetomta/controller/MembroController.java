package com.projetomta.controller;

import com.projetomta.dto.MembroRequest;
import com.projetomta.dto.MembroResponse;
import com.projetomta.service.MembroService;
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
@RequestMapping("/api/membros")
@RequiredArgsConstructor
public class MembroController {

    private final MembroService membroService;

    // 🔒 CORRIGIDO: apenas ADMIN pode listar membros (dados sensíveis: nome, email, telefone)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MembroResponse>> listar(
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "nomeCompleto", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(membroService.listar(busca, pageable));
    }

    // 🔒 CORRIGIDO: apenas ADMIN pode buscar membro por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembroResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(membroService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembroResponse> criar(@Valid @RequestBody MembroRequest request) {
        MembroResponse response = membroService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembroResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MembroRequest request
    ) {
        return ResponseEntity.ok(membroService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        membroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
