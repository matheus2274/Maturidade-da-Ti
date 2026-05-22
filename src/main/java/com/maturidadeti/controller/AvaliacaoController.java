package com.maturidadeti.controller;

import com.maturidadeti.dto.DTOs.AvaliacaoDetalheResponse;
import com.maturidadeti.dto.DTOs.AvaliacaoResumoResponse;
import com.maturidadeti.dto.DTOs.FinalizarRequest;
import com.maturidadeti.dto.DTOs.RelatorioEstatisticasResponse;
import com.maturidadeti.dto.DTOs.RespostaRequest;
import com.maturidadeti.dto.DTOs.RespostaResumoResponse;
import com.maturidadeti.entity.Avaliacao;
import com.maturidadeti.entity.Empresa;
import com.maturidadeti.entity.Questao;
import com.maturidadeti.entity.RespostaAvaliacao;
import com.maturidadeti.entity.Usuario;
import com.maturidadeti.repository.AvaliacaoRepository;
import com.maturidadeti.repository.EmpresaRepository;
import com.maturidadeti.repository.QuestaoRepository;
import com.maturidadeti.repository.RespostaAvaliacaoRepository;
import com.maturidadeti.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoRepository avaliacaoRepository;
    private final EmpresaRepository empresaRepository;
    private final QuestaoRepository questaoRepository;
    private final RespostaAvaliacaoRepository respostaRepository;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/iniciar")
    public ResponseEntity<AvaliacaoResumoResponse> iniciar(@RequestParam Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa nao encontrada"));

        Usuario avaliador = getUsuarioLogado();
        Avaliacao avaliacao = Avaliacao.builder()
                .empresa(empresa)
                .avaliador(avaliador)
                .status(Avaliacao.Status.EM_ANDAMENTO)
                .build();

        return ResponseEntity.ok(toResumo(avaliacaoRepository.save(avaliacao)));
    }

    @PostMapping("/{id}/responder")
    public ResponseEntity<RespostaResumoResponse> responder(@PathVariable Long id,
                                                            @Valid @RequestBody RespostaRequest request) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliacao nao encontrada"));
        Questao questao = questaoRepository.findById(request.getQuestaoId())
                .orElseThrow(() -> new IllegalArgumentException("Questao nao encontrada"));

        RespostaAvaliacao resposta = respostaRepository
                .findByAvaliacaoIdAndQuestaoId(id, request.getQuestaoId())
                .orElse(new RespostaAvaliacao());

        resposta.setAvaliacao(avaliacao);
        resposta.setQuestao(questao);
        resposta.setResposta(parseResposta(request.getResposta()));
        resposta.setObservacao(request.getObservacao());
        resposta.setRespondidaEm(LocalDateTime.now());

        return ResponseEntity.ok(toRespostaResumo(respostaRepository.save(resposta)));
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<AvaliacaoDetalheResponse> finalizar(@PathVariable Long id,
                                                              @RequestBody FinalizarRequest request) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliacao nao encontrada"));

        List<RespostaAvaliacao> respostas = carregarRespostasOrdenadas(id);
        double score = calcularScore(respostas);
        String nivel = calcularNivel(score);

        avaliacao.setStatus(Avaliacao.Status.FINALIZADA);
        avaliacao.setScoreGeral(score);
        avaliacao.setNivelMaturidade(nivel);
        avaliacao.setObservacoes(request != null ? request.getObservacoes() : null);
        avaliacao.setFinalizadaEm(LocalDateTime.now());

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        return ResponseEntity.ok(toDetalhe(salva, respostas));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoDetalheResponse> buscar(@PathVariable Long id) {
        return avaliacaoRepository.findById(id)
                .map(avaliacao -> ResponseEntity.ok(toDetalhe(avaliacao, carregarRespostasOrdenadas(id))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<AvaliacaoResumoResponse>> listarPorEmpresa(@PathVariable Long empresaId) {
        return ResponseEntity.ok(avaliacaoRepository.findByEmpresaIdOrderByIniciadaEmDesc(empresaId).stream()
                .map(this::toResumo)
                .toList());
    }

    @GetMapping("/minhas-avaliacoes")
    public ResponseEntity<List<AvaliacaoResumoResponse>> minhasAvaliacoes() {
        Usuario usuario = getUsuarioLogado();
        return ResponseEntity.ok(avaliacaoRepository.findByAvaliadorId(usuario.getId()).stream()
                .map(this::toResumo)
                .toList());
    }

    @GetMapping("/{id}/relatorio")
    public ResponseEntity<AvaliacaoDetalheResponse> relatorio(@PathVariable Long id) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliacao nao encontrada"));
        return ResponseEntity.ok(toDetalhe(avaliacao, carregarRespostasOrdenadas(id)));
    }

    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<RelatorioEstatisticasResponse> estatisticas(@PathVariable Long id) {
        Avaliacao avaliacao = avaliacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Avaliacao nao encontrada"));
        List<RespostaAvaliacao> respostas = carregarRespostasOrdenadas(id);
        long ok = respostas.stream().filter(r -> r.getResposta() == RespostaAvaliacao.Resposta.OK).count();
        long parcial = respostas.stream().filter(r -> r.getResposta() == RespostaAvaliacao.Resposta.PARCIAL).count();
        long naoOk = respostas.stream().filter(r -> r.getResposta() == RespostaAvaliacao.Resposta.NAO_OK).count();

        return ResponseEntity.ok(new RelatorioEstatisticasResponse(
                avaliacao.getId(),
                (long) respostas.size(),
                ok,
                parcial,
                naoOk,
                avaliacao.getScoreGeral(),
                avaliacao.getNivelMaturidade()));
    }

    private List<RespostaAvaliacao> carregarRespostasOrdenadas(Long avaliacaoId) {
        return respostaRepository.findByAvaliacaoId(avaliacaoId).stream()
                .sorted(Comparator.comparing(r -> r.getQuestao().getId()))
                .toList();
    }

    private double calcularScore(List<RespostaAvaliacao> respostas) {
        if (respostas.isEmpty()) {
            return 0;
        }

        double total = respostas.stream().mapToDouble(r -> switch (r.getResposta()) {
            case OK -> 100;
            case PARCIAL -> 50;
            case NAO_OK -> 0;
        }).sum();
        return Math.round((total / respostas.size()) * 10.0) / 10.0;
    }

    private String calcularNivel(double score) {
        if (score < 40) {
            return "ARTESANAL";
        }
        if (score < 60) {
            return "EFICIENTE";
        }
        if (score < 80) {
            return "EFETIVA";
        }
        return "ESTRATEGICA";
    }

    private RespostaAvaliacao.Resposta parseResposta(String resposta) {
        try {
            return RespostaAvaliacao.Resposta.valueOf(resposta.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Resposta invalida. Use OK, PARCIAL ou NAO_OK");
        }
    }

    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Usuario nao autenticado");
        }

        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado"));
    }

    private AvaliacaoResumoResponse toResumo(Avaliacao avaliacao) {
        return new AvaliacaoResumoResponse(
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
                avaliacao.getFinalizadaEm());
    }

    private AvaliacaoDetalheResponse toDetalhe(Avaliacao avaliacao, List<RespostaAvaliacao> respostas) {
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
                respostas.stream().map(this::toRespostaResumo).toList());
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
