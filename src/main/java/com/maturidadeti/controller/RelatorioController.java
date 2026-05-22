package com.maturidadeti.controller;

import com.maturidadeti.dto.DTOs.AvaliacaoDetalheResponse;
import com.maturidadeti.dto.DTOs.RelatorioEstatisticasResponse;
import com.maturidadeti.dto.DTOs.RespostaResumoResponse;
import com.maturidadeti.entity.Avaliacao;
import com.maturidadeti.entity.Questao;
import com.maturidadeti.entity.RespostaAvaliacao;
import com.maturidadeti.entity.Usuario;
import com.maturidadeti.repository.AvaliacaoRepository;
import com.maturidadeti.repository.RespostaAvaliacaoRepository;
import com.maturidadeti.service.RelatorioExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final AvaliacaoRepository avaliacaoRepository;
    private final RespostaAvaliacaoRepository respostaRepository;
    private final RelatorioExportService relatorioExportService;

    @GetMapping("/{id}/html")
    public ResponseEntity<String> exportarHtml(@PathVariable Long id) {
        AvaliacaoDetalheResponse avaliacao = carregarDetalhe(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(relatorioExportService.gerarHtml(avaliacao));
    }

    @GetMapping("/{id}/csv")
    public ResponseEntity<byte[]> exportarCsv(@PathVariable Long id) {
        AvaliacaoDetalheResponse avaliacao = carregarDetalhe(id);
        byte[] body = relatorioExportService.gerarCsv(avaliacao).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("relatorio-avaliacao-" + id + ".csv")
                        .build()
                        .toString())
                .body(body);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Map<String, String>> exportarPdf(@PathVariable Long id) {
        carregarDetalhe(id);
        return ResponseEntity.status(501).body(Map.of(
                "message", "Exportacao PDF ainda nao implementada nesta versao do backend",
                "avaliacaoId", String.valueOf(id)));
    }

    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<RelatorioEstatisticasResponse> estatisticas(@PathVariable Long id) {
        AvaliacaoDetalheResponse avaliacao = carregarDetalhe(id);
        return ResponseEntity.ok(relatorioExportService.recalcularEstatisticas(avaliacao));
    }

    private AvaliacaoDetalheResponse carregarDetalhe(Long id) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliacao nao encontrada"));
        List<RespostaResumoResponse> respostas = respostaRepository.findByAvaliacaoId(id).stream()
                .sorted(Comparator.comparing(r -> r.getQuestao().getId()))
                .map(this::toRespostaResumo)
                .toList();

        return new AvaliacaoDetalheResponse(
                avaliacao.getId(),
                avaliacao.getEmpresa().getId(),
                avaliacao.getEmpresa().getRazaoSocial(),
                avaliacao.getAvaliador().getId(),
                nomeCompleto(avaliacao.getAvaliador()),
                avaliacao.getStatus().name(),
                avaliacao.getScoreGeral(),
                avaliacao.getNivelMaturidade(),
                avaliacao.getObservacoes(),
                avaliacao.getIniciadaEm(),
                avaliacao.getFinalizadaEm(),
                respostas);
    }

    private RespostaResumoResponse toRespostaResumo(RespostaAvaliacao resposta) {
        Questao questao = resposta.getQuestao();
        return new RespostaResumoResponse(
                resposta.getId(),
                questao.getId(),
                questao.getArea().name(),
                questao.getEnunciado(),
                questao.getFramework(),
                questao.getCriticidade() != null ? questao.getCriticidade().name() : null,
                resposta.getResposta().name(),
                resposta.getObservacao(),
                resposta.getRespondidaEm());
    }

    private String nomeCompleto(Usuario usuario) {
        String sobrenome = usuario.getSobrenome() == null ? "" : usuario.getSobrenome().trim();
        return (usuario.getNome() + " " + sobrenome).trim();
    }
}
