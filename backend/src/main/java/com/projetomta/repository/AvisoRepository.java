package com.projetomta.repository;

import com.projetomta.domain.entity.Aviso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    Page<Aviso> findByAtivoTrue(Pageable pageable);
}
