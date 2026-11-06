package com.cti.provedor.service;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.dto.UsuarioDto;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Descobre quem está logado, lista usuários e muda o status. */
@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    /** O próprio usuário troca a senha: precisa acertar a senha atual. */
    public void trocarSenha(Usuario usuario, String senhaAtual, String novaSenha) {
        if (!encoder.matches(senhaAtual, usuario.getSenhaHash()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "A senha atual está incorreta.");
        if (encoder.matches(novaSenha, usuario.getSenhaHash()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "A nova senha precisa ser diferente da atual.");
        salvarSenha(usuario, novaSenha);
    }

    /** O admin define uma senha nova para outro usuário (ex.: quem esqueceu a senha). */
    public UsuarioDto redefinirSenha(Long id, String novaSenha, Usuario quemAltera) {
        Usuario alvo = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (alvo.getId().equals(quemAltera.getId()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Para trocar a sua própria senha, use Configurações.");
        salvarSenha(alvo, novaSenha);
        return UsuarioDto.de(alvo);
    }

    private void salvarSenha(Usuario u, String novaSenha) {
        if (novaSenha.getBytes(StandardCharsets.UTF_8).length > 72)  // limite do BCrypt
            throw new ApiException(HttpStatus.BAD_REQUEST, "A senha é longa demais (máximo de 72 bytes).");
        u.setSenhaHash(encoder.encode(novaSenha));  // CRIPTOGRAFADA, nunca pura
        repo.save(u);
    }

    public Usuario atual(Authentication authentication) {  // o e-mail do token vira busca no banco
        if (authentication == null)
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida. Faça login novamente.");
        return repo.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Sessão inválida. Faça login novamente."));
    }

    public List<UsuarioDto> listar(StatusUsuario status) {
        List<Usuario> lista = status == null ? repo.findAllByOrderByCriadoEmDesc()
                : repo.findByStatusOrderByCriadoEmDesc(status);
        return lista.stream().map(UsuarioDto::de).toList();  // sem senha
    }

    public UsuarioDto alterarStatus(Long id, StatusUsuario novoStatus, Usuario quemAltera) {
        Usuario alvo = repo.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        if (alvo.getId().equals(quemAltera.getId()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Você não pode alterar o seu próprio acesso.");
        alvo.setStatus(novoStatus);
        return UsuarioDto.de(repo.save(alvo));
    }
}
