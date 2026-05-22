package com.maturidadeti.controller;

import com.maturidadeti.dto.DTOs.EmpresaRequest;
import com.maturidadeti.dto.DTOs.ErroResponse;
import com.maturidadeti.entity.Empresa;
import com.maturidadeti.repository.EmpresaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaRepository empresaRepository;

    @GetMapping
    public ResponseEntity<List<Empresa>> listar() {
        return ResponseEntity.ok(empresaRepository.findAll());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<Empresa>> listarAtivas() {
        return ResponseEntity.ok(empresaRepository.findByStatus(Empresa.Status.ATIVO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> buscarPorId(@PathVariable Long id) {
        return empresaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody EmpresaRequest request) {
        if (empresaRepository.existsByCnpj(request.getCnpj())) {
            return ResponseEntity.badRequest().body(new ErroResponse(
                    LocalDateTime.now().toString(),
                    400,
                    "Bad Request",
                    "CNPJ ja cadastrado"));
        }

        Empresa empresa = Empresa.builder()
                .razaoSocial(request.getRazaoSocial())
                .cnpj(request.getCnpj())
                .setor(parseSetor(request.getSetor()))
                .porte(parsePorte(request.getPorte()))
                .responsavel(request.getResponsavel())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .observacoes(request.getObservacoes())
                .status(Empresa.Status.ATIVO)
                .build();
        return ResponseEntity.ok(empresaRepository.save(empresa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empresa> atualizar(@PathVariable Long id, @Valid @RequestBody EmpresaRequest request) {
        return empresaRepository.findById(id).map(empresa -> {
            empresa.setRazaoSocial(request.getRazaoSocial());
            empresa.setCnpj(request.getCnpj());
            empresa.setSetor(parseSetor(request.getSetor()));
            empresa.setPorte(parsePorte(request.getPorte()));
            empresa.setResponsavel(request.getResponsavel());
            empresa.setEmail(request.getEmail());
            empresa.setTelefone(request.getTelefone());
            empresa.setObservacoes(request.getObservacoes());
            return ResponseEntity.ok(empresaRepository.save(empresa));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return empresaRepository.findById(id).map(empresa -> {
            empresa.setStatus(Empresa.Status.INATIVO);
            empresaRepository.save(empresa);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private Empresa.Setor parseSetor(String value) {
        try {
            return Empresa.Setor.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Setor invalido");
        }
    }

    private Empresa.Porte parsePorte(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Empresa.Porte.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Porte invalido");
        }
    }
}
