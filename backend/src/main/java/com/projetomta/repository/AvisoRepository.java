package com.projetomta.repository;

import com.projetomta.domain.entity.Aviso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    Page<Aviso> findByAtivoTrue(Pageable pageable);

    @Query("""
            SELECT a FROM Aviso a
            WHERE a.ativo = true
              AND (a.dataExpiracao IS NULL OR a.dataExpiracao >= :agora)
            """)
    Page<Aviso> findAtivosVigentes(@Param("agora") LocalDateTime agora, Pageable pageable);

    @Modifying
    int deleteByDataExpiracaoIsNotNullAndDataExpiracaoBefore(LocalDateTime limite);
}
