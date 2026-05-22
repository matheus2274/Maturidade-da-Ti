package com.maturidadeti.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// ── Login ──
@Data
class LoginDTO {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String senha;
}

// ── Cadastro ──
@Data
class CadastroDTO {
    @NotBlank
    private String nome;
    @NotBlank
    private String sobrenome;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String senha;
    @NotBlank
    private String perfil;
}

// ── AuthResponse ──
@Data
class AuthResponseDTO {
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String email;
    private String nome;
    private String role;

    public AuthResponseDTO(String token, Long id, String email, String nome, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nome = nome;
        this.role = role;
    }
}
