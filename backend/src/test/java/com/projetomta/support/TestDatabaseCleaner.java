package com.projetomta.support;

import com.projetomta.repository.AvisoRepository;
import com.projetomta.repository.EscalaRepository;
import com.projetomta.repository.EventoRepository;
import com.projetomta.repository.MembroRepository;
import com.projetomta.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDatabaseCleaner {

    @Autowired
    private EscalaRepository escalaRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private AvisoRepository avisoRepository;

    @Autowired
    private MembroRepository membroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void limparTudo() {
        escalaRepository.deleteAll();
        eventoRepository.deleteAll();
        avisoRepository.deleteAll();
        membroRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
}
