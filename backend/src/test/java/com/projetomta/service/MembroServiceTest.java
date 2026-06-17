package com.projetomta.service;

import com.projetomta.domain.entity.Membro;
import com.projetomta.dto.MembroRequest;
import com.projetomta.exception.RegraNegocioException;
import com.projetomta.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroServiceTest {

    @Mock
    private MembroRepository membroRepository;

    @InjectMocks
    private MembroService membroService;

    @Test
    void deveCriarMembroComEmailUnico() {
        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("João Silva");
        request.setEmail("joao@test.com");

        when(membroRepository.existsByEmailIgnoreCase("joao@test.com")).thenReturn(false);
        when(membroRepository.save(any(Membro.class))).thenAnswer(invocation -> {
            Membro membro = invocation.getArgument(0);
            membro.setId(1L);
            return membro;
        });

        var response = membroService.criar(request);

        assertEquals("João Silva", response.getNomeCompleto());
        assertEquals("joao@test.com", response.getEmail());

        ArgumentCaptor<Membro> captor = ArgumentCaptor.forClass(Membro.class);
        verify(membroRepository).save(captor.capture());
        assertEquals("joao@test.com", captor.getValue().getEmail());
    }

    @Test
    void deveRejeitarEmailDuplicadoAoCriar() {
        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("Maria");
        request.setEmail("duplicado@test.com");

        when(membroRepository.existsByEmailIgnoreCase("duplicado@test.com")).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> membroService.criar(request));
        verify(membroRepository, never()).save(any());
    }

    @Test
    void devePermitirVariosMembrosSemEmail() {
        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("Sem Email");

        when(membroRepository.save(any(Membro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        membroService.criar(request);

        verify(membroRepository, never()).existsByEmailIgnoreCase(any());
        verify(membroRepository).save(any(Membro.class));
    }

    @Test
    void deveRejeitarEmailDuplicadoAoAtualizar() {
        MembroRequest request = new MembroRequest();
        request.setNomeCompleto("Pedro");
        request.setEmail("existente@test.com");

        Membro existente = Membro.builder().id(2L).nomeCompleto("Pedro").email("outro@test.com").ativo(true).build();

        when(membroRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(membroRepository.existsByEmailIgnoreCaseAndIdNot("existente@test.com", 2L)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> membroService.atualizar(2L, request));
    }
}
