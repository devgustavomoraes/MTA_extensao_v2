package com.projetomta.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtSecretValidatorTest {

    @Test
    void deveAceitarSecretForteEmProducao() {
        JwtSecretValidator validator = criarValidator(
                "uma-chave-secreta-forte-com-mais-de-trinta-e-dois-bytes!!",
                "prod"
        );
        assertDoesNotThrow(validator::validarSegredoJwt);
    }

    @Test
    void deveRejeitarSecretPadraoEmProducao() {
        JwtSecretValidator validator = criarValidator(
                "changeme-local-dev-only-min-32-chars!!",
                "prod"
        );
        assertThrows(IllegalStateException.class, validator::validarSegredoJwt);
    }

    @Test
    void devePermitirSecretPadraoEmDesenvolvimento() {
        JwtSecretValidator validator = criarValidator(
                "changeme-local-dev-only-min-32-chars!!",
                "dev"
        );
        assertDoesNotThrow(validator::validarSegredoJwt);
    }

    private JwtSecretValidator criarValidator(String secret, String profile) {
        AppProperties props = new AppProperties();
        AppProperties.Jwt jwt = new AppProperties.Jwt();
        jwt.setSecret(secret);
        props.setJwt(jwt);

        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{profile});

        JwtSecretValidator validator = new JwtSecretValidator(props, environment);
        return validator;
    }
}
