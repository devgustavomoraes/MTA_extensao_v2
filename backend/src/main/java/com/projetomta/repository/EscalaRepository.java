package com.projetomta.repository;

import com.projetomta.domain.entity.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EscalaRepository extends JpaRepository<Escala, Long> {

    List<Escala> findByEventoId(Long eventoId);

    Optional<Escala> findByIdAndEventoId(Long id, Long eventoId);

    boolean existsByEventoIdAndMembroId(Long eventoId, Long membroId);

    @Query("""
            SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END
            FROM Escala es
            JOIN es.evento e
            WHERE es.membro.id = :membroId
              AND (:excludeEventoId IS NULL OR e.id <> :excludeEventoId)
              AND e.dataInicio < :dataFim
              AND e.dataFim > :dataInicio
            """)
    boolean existsConflitoMembro(
            @Param("membroId") Long membroId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("excludeEventoId") Long excludeEventoId
    );
}
