package com.projetomta.repository;

import com.projetomta.domain.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
            FROM Evento e
            WHERE (:excludeId IS NULL OR e.id <> :excludeId)
              AND e.dataInicio < :dataFim
              AND e.dataFim > :dataInicio
            """)
    boolean existsConflitoHorario(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim,
            @Param("excludeId") Long excludeId
    );

    Page<Evento> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

    List<Evento> findTop10ByDataInicioGreaterThanEqualOrderByDataInicioAsc(LocalDateTime agora);
}
