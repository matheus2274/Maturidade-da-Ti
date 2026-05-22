package com.maturidadeti.repository;

import com.maturidadeti.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    List<Empresa> findByStatus(Empresa.Status status);
    boolean existsByCnpj(String cnpj);
}
