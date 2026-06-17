package com.projetomta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EscalaRequest {

    @NotNull(message = "Membro é obrigatório")
    private Long membroId;

    @Size(max = 100, message = "Função na escala deve ter no máximo 100 caracteres")
    private String funcaoEscala;

    private Boolean confirmado;
}
