package com.maturidadeti.service;

import com.maturidadeti.dto.DTOs.AvaliacaoDetalheResponse;
import com.maturidadeti.dto.DTOs.RelatorioEstatisticasResponse;
import com.maturidadeti.dto.DTOs.RespostaResumoResponse;
import org.springframework.stereotype.Service;

@Service
public class RelatorioExportService {

    public String gerarHtml(AvaliacaoDetalheResponse avaliacao) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"pt-BR\"><head><meta charset=\"UTF-8\">");
        html.append("<title>Relatorio de Avaliacao</title>");
        html.append("<style>");
        html.append("body{font-family:Arial,sans-serif;margin:32px;color:#1f2937;}");
        html.append("h1,h2{margin-bottom:8px;}table{width:100%;border-collapse:collapse;margin-top:16px;}");
        html.append("th,td{border:1px solid #d1d5db;padding:10px;text-align:left;vertical-align:top;}");
        html.append("th{background:#f3f4f6;} .meta{margin:4px 0;} .badge{display:inline-block;padding:4px 8px;background:#e5f3ff;border-radius:4px;}");
        html.append("</style></head><body>");
        html.append("<h1>Relatorio de Avaliacao</h1>");
        html.append("<div class=\"meta\"><strong>Empresa:</strong> ").append(escape(avaliacao.getEmpresaNome())).append("</div>");
        html.append("<div class=\"meta\"><strong>Avaliador:</strong> ").append(escape(avaliacao.getAvaliadorNome())).append("</div>");
        html.append("<div class=\"meta\"><strong>Status:</strong> ").append(escape(avaliacao.getStatus())).append("</div>");
        html.append("<div class=\"meta\"><strong>Score:</strong> ").append(avaliacao.getScoreTotal() == null ? 0 : avaliacao.getScoreTotal()).append("</div>");
        html.append("<div class=\"meta\"><strong>Nivel:</strong> <span class=\"badge\">").append(escape(avaliacao.getNivel())).append("</span></div>");

        if (avaliacao.getObservacoes() != null && !avaliacao.getObservacoes().isBlank()) {
            html.append("<h2>Observacoes Gerais</h2><p>").append(escape(avaliacao.getObservacoes())).append("</p>");
        }

        html.append("<h2>Respostas</h2><table><thead><tr>");
        html.append("<th>Area</th><th>Questao</th><th>Resposta</th><th>Observacao</th>");
        html.append("</tr></thead><tbody>");

        for (RespostaResumoResponse resposta : avaliacao.getRespostas()) {
            html.append("<tr>");
            html.append("<td>").append(escape(resposta.getArea())).append("</td>");
            html.append("<td>").append(escape(resposta.getEnunciado())).append("</td>");
            html.append("<td>").append(escape(resposta.getResposta())).append("</td>");
            html.append("<td>").append(escape(resposta.getObservacao())).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table></body></html>");
        return html.toString();
    }

    public String gerarCsv(AvaliacaoDetalheResponse avaliacao) {
        StringBuilder csv = new StringBuilder();
        csv.append("avaliacaoId,empresa,avaliador,area,questao,resposta,observacao,respondidaEm\n");
        for (RespostaResumoResponse resposta : avaliacao.getRespostas()) {
            csv.append(csv(avaliacao.getId())).append(",");
            csv.append(csv(avaliacao.getEmpresaNome())).append(",");
            csv.append(csv(avaliacao.getAvaliadorNome())).append(",");
            csv.append(csv(resposta.getArea())).append(",");
            csv.append(csv(resposta.getEnunciado())).append(",");
            csv.append(csv(resposta.getResposta())).append(",");
            csv.append(csv(resposta.getObservacao())).append(",");
            csv.append(csv(resposta.getRespondidaEm() == null ? "" : resposta.getRespondidaEm().toString())).append("\n");
        }
        return csv.toString();
    }

    public RelatorioEstatisticasResponse recalcularEstatisticas(AvaliacaoDetalheResponse avaliacao) {
        long ok = avaliacao.getRespostas().stream().filter(r -> "OK".equals(r.getResposta())).count();
        long parcial = avaliacao.getRespostas().stream().filter(r -> "PARCIAL".equals(r.getResposta())).count();
        long naoOk = avaliacao.getRespostas().stream().filter(r -> "NAO_OK".equals(r.getResposta())).count();

        return new RelatorioEstatisticasResponse(
                avaliacao.getId(),
                (long) avaliacao.getRespostas().size(),
                ok,
                parcial,
                naoOk,
                avaliacao.getScoreTotal(),
                avaliacao.getNivel());
    }

    private String csv(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
