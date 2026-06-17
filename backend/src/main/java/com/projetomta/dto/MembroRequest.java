package com.projetomta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembroRequest {

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 255, message = "Nome completo deve ter no máximo 255 caracteres")
    private String nomeCompleto;

    @Email(message = "E-mail inválido")
    @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
    private String email;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Size(max = 100, message = "Função deve ter no máximo 100 caracteres")
    private String funcao;

    private String observacoes;

    private Boolean ativo;
}
