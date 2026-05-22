package com.maturidadeti.repository;

import com.maturidadeti.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {
    List<Avaliacao> findByEmpresaId(Long empresaId);
    List<Avaliacao> findByAvaliadorId(Long avaliadorId);
    List<Avaliacao> findByEmpresaIdOrderByIniciadaEmDesc(Long empresaId);
}
