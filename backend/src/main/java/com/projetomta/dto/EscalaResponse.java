package com.projetomta.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EscalaResponse {

    private Long id;
    private Long eventoId;
    private Long membroId;
    private String membroNome;
    private String funcaoEscala;
    private Boolean confirmado;
    private LocalDateTime criadoEm;
}
