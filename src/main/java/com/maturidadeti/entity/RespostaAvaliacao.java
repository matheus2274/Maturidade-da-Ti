package com.maturidadeti.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "respostas_avaliacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespostaAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    @ToString.Exclude
    private Avaliacao avaliacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "questao_id", nullable = false)
    private Questao questao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Resposta resposta;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    @PrePersist
    protected void onCreate() {
        respondidaEm = LocalDateTime.now();
    }

    public enum Resposta {
        OK, PARCIAL, NAO_OK
    }
}
