package com.maturidadeti.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "questoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Area area;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    private String framework;

    @Enumerated(EnumType.STRING)
    private Criticidade criticidade;

    @Column(name = "evidencia_esperada", columnDefinition = "TEXT")
    private String evidenciaEsperada;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public enum Area {
        GOVERNANCA, SEGURANCA, INFRAESTRUTURA, DESENVOLVIMENTO, SUPORTE, RISCO
    }

    public enum Criticidade {
        BAIXA, MEDIA, ALTA
    }
}
