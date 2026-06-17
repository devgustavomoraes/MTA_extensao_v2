package com.projetomta.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventoResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private String local;
    private Long criadoPorId;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
