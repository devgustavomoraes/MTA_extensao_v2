package com.projetomta.service;

import com.projetomta.config.ExpiracaoProperties;
import com.projetomta.repository.AvisoRepository;
import com.projetomta.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpiracaoCleanupService {

    private final EventoRepository eventoRepository;
    private final AvisoRepository avisoRepository;
    private final ExpiracaoProperties expiracaoProperties;

    /** Remove fisicamente registros encerrados há mais de {@code retencaoDias} (após período de consulta do admin). */
    @Scheduled(cron = "${app.expiracao.limpeza-cron:0 0 * * * *}")
    @Transactional
    public void limparRegistrosExpirados() {
        LocalDateTime limite = LocalDateTime.now().minusDays(expiracaoProperties.getRetencaoDias());

        int eventos = eventoRepository.deleteByDataFimBefore(limite);
        int avisos = avisoRepository.deleteByDataExpiracaoIsNotNullAndDataExpiracaoBefore(limite);

        if (eventos > 0 || avisos > 0) {
            log.info("Limpeza de expirados: {} evento(s) e {} aviso(s) removidos (anteriores a {})",
                    eventos, avisos, limite);
        }
    }
}
