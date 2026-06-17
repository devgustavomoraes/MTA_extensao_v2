package com.projetomta.exception;

import java.time.LocalDateTime;

public class ContaBloqueadaException extends RuntimeException {

    private final LocalDateTime bloqueadoAte;

    public ContaBloqueadaException(LocalDateTime bloqueadoAte) {
        super("Conta temporariamente bloqueada por excesso de tentativas de login");
        this.bloqueadoAte = bloqueadoAte;
    }

    public LocalDateTime getBloqueadoAte() {
        return bloqueadoAte;
    }
}
