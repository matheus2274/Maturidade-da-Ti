package com.maturidadeti.repository;

import com.maturidadeti.entity.Questao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestaoRepository extends JpaRepository<Questao, Long> {
    List<Questao> findByAtivaTrue();
    List<Questao> findByAreaAndAtivaTrue(Questao.Area area);
}
