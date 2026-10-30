package com.cti.provedor.service;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.dto.UsuarioDto;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Descobre quem está logado, lista usuários e muda o status. */
@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
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
