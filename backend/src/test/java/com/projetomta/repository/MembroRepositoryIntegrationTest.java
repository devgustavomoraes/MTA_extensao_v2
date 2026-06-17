package com.projetomta.repository;

import com.projetomta.domain.entity.Membro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class MembroRepositoryIntegrationTest {

    @Autowired
    private MembroRepository membroRepository;

    @Test
    void deveDetectarEmailDuplicadoIgnorandoMaiusculas() {
        membroRepository.save(Membro.builder()
                .nomeCompleto("Ana")
                .email("ana@test.com")
                .ativo(true)
                .build());

        assertTrue(membroRepository.existsByEmailIgnoreCase("ANA@TEST.COM"));
        assertFalse(membroRepository.existsByEmailIgnoreCase("outro@test.com"));
    }

    @Test
    void deveIgnorarProprioIdNaVerificacaoDeDuplicidade() {
        Membro membro = membroRepository.save(Membro.builder()
                .nomeCompleto("Carlos")
                .email("carlos@test.com")
                .ativo(true)
                .build());

        assertFalse(membroRepository.existsByEmailIgnoreCaseAndIdNot("carlos@test.com", membro.getId()));
        assertTrue(membroRepository.existsByEmailIgnoreCaseAndIdNot("carlos@test.com", 999L));
    }
}
