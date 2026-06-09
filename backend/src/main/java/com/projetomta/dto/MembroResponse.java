package com.projetomta.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MembroResponse {

    private Long id;
    private String nomeCompleto;
    private String email;
    private String telefone;
    private String funcao;
    private Boolean ativo;
    private String observacoes;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
