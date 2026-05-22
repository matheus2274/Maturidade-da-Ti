package com.maturidadeti.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class DTOs {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String senha;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CadastroRequest {
        @NotBlank private String nome;
        @NotBlank private String sobrenome;
        @NotBlank @Email private String email;
        @NotBlank @Size(min = 8) private String senha;
        @NotBlank private String perfil;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String tipo = "Bearer";
        private Long id;
        private String email;
        private String nome;
        private String role;

        public AuthResponse(String token, Long id, String email, String nome, String role) {
            this.token = token; this.id = id;
            this.email = email; this.nome = nome; this.role = role;
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class EmpresaRequest {
        @NotBlank private String razaoSocial;
        @NotBlank private String cnpj;
        @NotBlank private String setor;
        private String porte;
        @NotBlank private String responsavel;
        @NotBlank @Email private String email;
        private String telefone;
        private String observacoes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class QuestaoRequest {
        @NotBlank private String area;
        @NotBlank private String enunciado;
        private String framework;
        private String criticidade;
        private String evidenciaEsperada;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RespostaRequest {
        @NotNull private Long questaoId;
        @NotBlank private String resposta;
        private String observacao;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class FinalizarRequest {
        private String observacoes;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ErroResponse {
        private String timestamp;
        private int status;
        private String error;
        private String message;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AvaliacaoResumoResponse {
        private Long id;
        private Long empresaId;
        private String empresaNome;
        private Long avaliadorId;
        private String avaliadorNome;
        private String status;
        private Double scoreTotal;
        private String nivel;
        private String observacoes;
        private LocalDateTime iniciadaEm;
        private LocalDateTime finalizadaEm;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RespostaResumoResponse {
        private Long id;
        private Long questaoId;
        private String area;
        private String enunciado;
        private String framework;
        private String criticidade;
        private String resposta;
        private String observacao;
        private LocalDateTime respondidaEm;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class AvaliacaoDetalheResponse {
        private Long id;
        private Long empresaId;
        private String empresaNome;
        private Long avaliadorId;
        private String avaliadorNome;
        private String status;
        private Double scoreTotal;
        private String nivel;
        private String observacoes;
        private LocalDateTime iniciadaEm;
        private LocalDateTime finalizadaEm;
        private List<RespostaResumoResponse> respostas;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class RelatorioEstatisticasResponse {
        private Long avaliacaoId;
        private Long totalQuestoes;
        private Long totalOk;
        private Long totalParcial;
        private Long totalNaoOk;
        private Double scoreTotal;
        private String nivel;
    }
}
