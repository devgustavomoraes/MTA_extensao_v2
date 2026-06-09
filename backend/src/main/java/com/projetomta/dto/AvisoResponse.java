package com.projetomta.dto;

import com.projetomta.domain.enums.PrioridadeAviso;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AvisoResponse {

    private Long id;
    private String titulo;
    private String mensagem;
    private PrioridadeAviso prioridade;
    private Boolean ativo;
    private Long publicadoPorId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
