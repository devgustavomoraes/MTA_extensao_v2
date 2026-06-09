package com.projetomta.dto;

import com.projetomta.domain.enums.Perfil;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String tipo;
    private Long id;
    private String email;
    private Perfil perfil;
}
