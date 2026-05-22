package com.maturidadeti.controller;

import com.maturidadeti.dto.DTOs.AuthResponse;
import com.maturidadeti.dto.DTOs.CadastroRequest;
import com.maturidadeti.dto.DTOs.ErroResponse;
import com.maturidadeti.dto.DTOs.LoginRequest;
import com.maturidadeti.entity.Usuario;
import com.maturidadeti.repository.UsuarioRepository;
import com.maturidadeti.security.JwtProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtProvider.gerarToken(authentication);
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail()).orElseThrow();
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    usuario.getId(),
                    usuario.getEmail(),
                    montarNomeCompleto(usuario),
                    usuario.getRole().name()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErroResponse(
                    LocalDateTime.now().toString(),
                    401,
                    "Unauthorized",
                    "Credenciais invalidas"));
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastro(@Valid @RequestBody CadastroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new ErroResponse(
                    LocalDateTime.now().toString(),
                    400,
                    "Bad Request",
                    "E-mail ja cadastrado"));
        }

        Usuario.Role role;
        try {
            role = Usuario.Role.valueOf(request.getPerfil().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErroResponse(
                    LocalDateTime.now().toString(),
                    400,
                    "Bad Request",
                    "Perfil invalido. Use um destes valores: " + Arrays.toString(Usuario.Role.values())));
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .sobrenome(request.getSobrenome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(role)
                .ativo(true)
                .build();
        usuarioRepository.save(usuario);

        String token = jwtProvider.gerarTokenPorEmail(usuario.getEmail());
        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getId(),
                usuario.getEmail(),
                montarNomeCompleto(usuario),
                usuario.getRole().name()));
    }

    private String montarNomeCompleto(Usuario usuario) {
        String sobrenome = usuario.getSobrenome() == null ? "" : usuario.getSobrenome().trim();
        return (usuario.getNome() + " " + sobrenome).trim();
    }
}
