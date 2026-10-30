package com.cti.provedor.config;

import com.cti.provedor.model.PerfilUsuario;
import com.cti.provedor.model.StatusUsuario;
import com.cti.provedor.model.Usuario;
import com.cti.provedor.repository.UsuarioRepository;
import com.cti.provedor.service.Datas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Se ainda não existe, cria o ADMIN com o e-mail e a senha do .env. Ele aprova os demais. */
@Component
public class AdminInicial implements ApplicationRunner {  // roda quando o servidor liga

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final AppProperties props;

    public AdminInicial(UsuarioRepository repo, PasswordEncoder encoder, AppProperties props) {
        this.repo = repo;
        this.encoder = encoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        var cfg = props.admin();
        if (cfg == null || cfg.email() == null || cfg.email().isBlank()) return;
        String email = cfg.email().trim().toLowerCase();
        if (repo.existsByEmailIgnoreCase(email)) return;  // já existe: não cria de novo
        if (cfg.senha() == null || cfg.senha().isBlank()) {
            log.warn("Administrador inicial NÃO criado: defina ADMIN_PASSWORD no backend/.env");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(cfg.nome() == null || cfg.nome().isBlank() ? "Administrador" : cfg.nome().trim());
        admin.setEmail(email);
        admin.setSenhaHash(encoder.encode(cfg.senha()));
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setStatus(StatusUsuario.APROVADO);  // já nasce aprovado
        admin.setCriadoEm(Datas.agoraUtc());
        repo.save(admin);
        log.warn("Administrador inicial criado: {} (troque a senha via ADMIN_PASSWORD no .env)", email);
    }
}
