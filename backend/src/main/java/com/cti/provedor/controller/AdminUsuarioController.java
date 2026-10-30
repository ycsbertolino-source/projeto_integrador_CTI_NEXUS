package com.cti.provedor.controller;

import com.cti.provedor.dto.UsuarioDto;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** GET lista os usuários; PATCH aprova ou bloqueia um deles. */
@RestController
@RequestMapping("/api/admin/usuarios")  // só ADMIN entra (SecurityConfig)
public class AdminUsuarioController {

    public record AlterarStatusRequest(
            @NotNull(message = "Informe o status") StatusUsuario status) {}  // { "status": "APROVADO" }

    private final UsuarioService service;

    public AdminUsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping  // GET /api/admin/usuarios?status=PENDENTE (opcional)
    public List<UsuarioDto> listar(@RequestParam(required = false) StatusUsuario status) {
        return service.listar(status);
    }

    @PatchMapping("/{id}/status")
    public UsuarioDto alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest req,
                                    Authentication authentication) {
        return service.alterarStatus(id, req.status(), service.atual(authentication));
    }
}
