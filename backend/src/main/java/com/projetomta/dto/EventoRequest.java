package com.projetomta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventoRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    private String titulo;

    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    private LocalDateTime dataFim;

    @Size(max = 255, message = "Local deve ter no máximo 255 caracteres")
    private String local;
}
