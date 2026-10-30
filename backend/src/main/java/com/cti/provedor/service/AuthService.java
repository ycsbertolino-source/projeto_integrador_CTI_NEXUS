package com.cti.provedor.service;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.config.JwtService;
import com.cti.provedor.dto.AuthDtos.CadastroRequest;
import com.cti.provedor.dto.AuthDtos.LoginRequest;
import com.cti.provedor.dto.AuthDtos.LoginResponse;
import com.cti.provedor.dto.AuthDtos.MensagemResponse;
import com.cti.provedor.dto.UsuarioDto;
import com.cti.provedor.model.PerfilUsuario;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository repo, PasswordEncoder encoder, JwtService jwt) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** Criptografa a senha com BCrypt e cria o usuário como USER e PENDENTE. */
    public MensagemResponse cadastrar(CadastroRequest req) {
        String email = req.email().trim().toLowerCase();
        if (req.senha().getBytes(StandardCharsets.UTF_8).length > 72)  // limite do BCrypt
            throw new ApiException(HttpStatus.BAD_REQUEST, "A senha é longa demais (máximo de 72 bytes).");
        if (repo.existsByEmailIgnoreCase(email))
            throw new ApiException(HttpStatus.CONFLICT, "Já existe um cadastro com este e-mail.");

        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(req.senha()));  // CRIPTOGRAFADA, nunca pura
        u.setMatricula(vazioParaNulo(req.matricula()));
        u.setTurma(vazioParaNulo(req.turma()));
        u.setPerfil(PerfilUsuario.USER);
        u.setStatus(StatusUsuario.PENDENTE);          // espera o admin liberar
        u.setCriadoEm(Datas.agoraUtc());
        repo.save(u);
        return new MensagemResponse("Cadastro recebido! Você poderá entrar assim que o administrador liberar o seu acesso.");
    }

    /** Confere a senha, depois o status, e só então devolve o token JWT. */
    public LoginResponse entrar(LoginRequest req) {
        Usuario u = repo.findByEmailIgnoreCase(req.email().trim()).orElse(null);
        // A senha é conferida antes do status, para não revelar quais e-mails existem.
        if (u == null || !encoder.matches(req.senha(), u.getSenhaHash()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        if (u.getStatus() == StatusUsuario.PENDENTE)
            throw new ApiException(HttpStatus.FORBIDDEN, "Seu cadastro ainda aguarda a liberação do administrador.");
        if (u.getStatus() == StatusUsuario.BLOQUEADO)
            throw new ApiException(HttpStatus.FORBIDDEN, "Seu acesso está bloqueado. Fale com o administrador.");
        return new LoginResponse(jwt.gerar(u), "Bearer", jwt.getExpiracaoSegundos(), UsuarioDto.de(u));
    }

    private static String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
