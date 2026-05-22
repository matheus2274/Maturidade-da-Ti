package com.maturidadeti.repository;

import com.maturidadeti.entity.RespostaAvaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RespostaAvaliacaoRepository extends JpaRepository<RespostaAvaliacao, Long> {
    List<RespostaAvaliacao> findByAvaliacaoId(Long avaliacaoId);
    Optional<RespostaAvaliacao> findByAvaliacaoIdAndQuestaoId(Long avaliacaoId, Long questaoId);
}
