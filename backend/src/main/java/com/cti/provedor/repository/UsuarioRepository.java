package com.cti.provedor.repository;

import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository já traz prontos: save, findById, findAll, deleteById ...
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // findByEmailIgnoreCase -> SELECT ... WHERE lower(email) = lower(?)
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findAllByOrderByCriadoEmDesc();

    List<Usuario> findByStatusOrderByCriadoEmDesc(StatusUsuario status);
}
