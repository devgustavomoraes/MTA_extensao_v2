package com.projetomta.dto;

import com.projetomta.domain.enums.PrioridadeAviso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvisoRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    private String titulo;

    @NotBlank(message = "Mensagem é obrigatória")
    private String mensagem;

    private PrioridadeAviso prioridade;

    private Boolean ativo;
}
