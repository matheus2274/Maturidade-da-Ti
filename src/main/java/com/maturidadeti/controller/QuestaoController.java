package com.maturidadeti.controller;

import com.maturidadeti.dto.DTOs.QuestaoRequest;
import com.maturidadeti.entity.Questao;
import com.maturidadeti.repository.QuestaoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/questoes")
@RequiredArgsConstructor
public class QuestaoController {

    private final QuestaoRepository questaoRepository;

    @GetMapping("/listar")
    public ResponseEntity<List<Questao>> listar() {
        return ResponseEntity.ok(questaoRepository.findByAtivaTrue());
    }

    @GetMapping("/area/{area}")
    public ResponseEntity<List<Questao>> listarPorArea(@PathVariable String area) {
        return ResponseEntity.ok(questaoRepository.findByAreaAndAtivaTrue(parseArea(area)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Questao> buscarPorId(@PathVariable Long id) {
        return questaoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Questao> criar(@Valid @RequestBody QuestaoRequest request) {
        Questao questao = Questao.builder()
                .area(parseArea(request.getArea()))
                .enunciado(request.getEnunciado())
                .framework(request.getFramework())
                .criticidade(parseCriticidade(request.getCriticidade()))
                .evidenciaEsperada(request.getEvidenciaEsperada())
                .ativa(true)
                .build();
        return ResponseEntity.ok(questaoRepository.save(questao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return questaoRepository.findById(id).map(questao -> {
            questao.setAtiva(false);
            questaoRepository.save(questao);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    private Questao.Area parseArea(String area) {
        try {
            return Questao.Area.valueOf(area.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Area invalida");
        }
    }

    private Questao.Criticidade parseCriticidade(String criticidade) {
        if (criticidade == null || criticidade.isBlank()) {
            return null;
        }
        try {
            return Questao.Criticidade.valueOf(criticidade.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Criticidade invalida");
        }
    }
}
