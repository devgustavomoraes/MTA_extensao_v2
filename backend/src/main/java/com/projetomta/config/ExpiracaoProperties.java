package com.projetomta.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.expiracao")
public class ExpiracaoProperties {

    /** Dias após o término em que o registro permanece visível ao admin antes da exclusão física. */
    private int retencaoDias = 7;
}
