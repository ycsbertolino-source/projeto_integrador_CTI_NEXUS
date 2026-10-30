package com.cti.provedor.dto;

import com.cti.provedor.model.Usuario;
import com.cti.provedor.service.Datas;
import java.time.Instant;

/** O que a API devolve do usuário (sem a senha!). */
public record UsuarioDto(Long id, String nome, String email, String matricula, String turma,
                         String perfil, String status, Instant criadoEm) {

    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNome(), u.getEmail(), u.getMatricula(), u.getTurma(),
                u.getPerfil().name(), u.getStatus().name(), Datas.paraInstant(u.getCriadoEm()));
    }
}
