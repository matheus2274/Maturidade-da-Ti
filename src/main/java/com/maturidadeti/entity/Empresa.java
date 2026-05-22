package com.maturidadeti.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "empresas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(nullable = false, unique = true)
    private String cnpj;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Setor setor;

    @Enumerated(EnumType.STRING)
    private Porte porte;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ATIVO;

    @Column(nullable = false)
    private String responsavel;

    @Column(nullable = false)
    private String email;

    private String telefone;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    private List<Avaliacao> avaliacoes;

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

    public enum Setor {
        TECNOLOGIA, SAUDE, FINANCEIRO, EDUCACAO, INDUSTRIA, COMERCIO, GOVERNO, OUTRO
    }

    public enum Porte {
        MICRO, PEQUENA, MEDIA, GRANDE
    }

    public enum Status {
        ATIVO, PENDENTE, INATIVO
    }
}
