package com.cti.provedor.controller;

import com.cti.provedor.dto.AuthDtos.CadastroRequest;
import com.cti.provedor.dto.AuthDtos.LoginRequest;
import com.cti.provedor.dto.AuthDtos.LoginResponse;
import com.cti.provedor.dto.AuthDtos.MensagemResponse;
import com.cti.provedor.dto.UsuarioDto;
import com.cti.provedor.service.AuthService;
import com.cti.provedor.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Quatro rotas: cadastro, login, "quem sou eu" e health. */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService auth;
    private final UsuarioService usuarios;

    public AuthController(AuthService auth, UsuarioService usuarios) {
        this.auth = auth;
        this.usuarios = usuarios;
    }

    @PostMapping("/auth/register") @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse cadastrar(@Valid @RequestBody CadastroRequest req) {
        return auth.cadastrar(req);  // cria o usuário como PENDENTE
    }

    @PostMapping("/auth/login")
    public LoginResponse entrar(@Valid @RequestBody LoginRequest req) {
        return auth.entrar(req);  // confere a senha e devolve o token JWT
    }

    @GetMapping("/auth/me")
    public UsuarioDto eu(Authentication authentication) {
        return UsuarioDto.de(usuarios.atual(authentication));
    }

    @GetMapping("/health")
    public Map<String, String> saude() { return Map.of("status", "ok"); }
}
