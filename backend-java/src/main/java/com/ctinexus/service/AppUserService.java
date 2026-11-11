package com.ctinexus.service;

import com.ctinexus.dto.AdminUserRequest;
import com.ctinexus.dto.ProfileUpdateRequest;
import com.ctinexus.dto.RegisterRequest;
import com.ctinexus.model.AppUser;
import com.ctinexus.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser authenticate(String email, String password) {
        AppUser user = repository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.");
        }
        if (!"Ativo".equals(user.status())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "A conta ainda não está ativa. Aguarde a aprovação de um administrador.");
        }
        repository.markSuccessfulLogin(user.id());
        return repository.findById(user.id()).orElseThrow();
    }

    public AppUser register(RegisterRequest request) {
        ensureEmailAvailable(request.email(), null);
        return repository.create(
                request.name().trim(),
                request.email(),
                passwordEncoder.encode(request.password()),
                "user",
                "Pendente"
        );
    }

    public Optional<AppUser> findActiveByEmail(String email) {
        return repository.findByEmail(email).filter(user -> "Ativo".equals(user.status()));
    }

    public AppUser findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    public List<Map<String, Object>> listUsers() {
        return repository.findAll().stream().map(this::publicUser).toList();
    }

    public AppUser createByAdmin(AdminUserRequest request) {
        String password = requirePassword(request.password());
        ensureEmailAvailable(request.email(), null);
        return repository.create(
                request.name().trim(),
                request.email(),
                passwordEncoder.encode(password),
                normalizeRole(request.role()),
                normalizeStatus(request.status())
        );
    }

    public AppUser updateByAdmin(long id, AdminUserRequest request) {
        AppUser existing = findById(id);
        String role = normalizeRole(request.role());
        String status = normalizeStatus(request.status());
        ensureLastAdminRemains(existing, role, status);
        ensureEmailAvailable(request.email(), id);
        String passwordHash = request.password() == null || request.password().isBlank()
                ? null
                : passwordEncoder.encode(requirePassword(request.password()));
        return repository.update(
                id,
                request.name().trim(),
                request.email(),
                passwordHash,
                role,
                status
        );
    }

    public AppUser updateOwnProfile(AppUser currentUser, ProfileUpdateRequest request) {
        ensureEmailAvailable(request.email(), currentUser.id());
        String passwordHash = null;
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null
                    || !passwordEncoder.matches(request.currentPassword(), currentUser.passwordHash())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual inválida.");
            }
            passwordHash = passwordEncoder.encode(requirePassword(request.newPassword()));
        }
        repository.updateProfile(currentUser.id(), request.name().trim(), request.email(), passwordHash);
        return findById(currentUser.id());
    }

    public void delete(long id) {
        AppUser target = findById(id);
        if ("admin".equals(target.role()) && "Ativo".equals(target.status())
                && repository.countActiveAdmins() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não é possível remover o último administrador ativo.");
        }
        repository.delete(id);
    }

    public Map<String, Object> publicUser(AppUser user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.id());
        result.put("name", user.name());
        result.put("email", user.email());
        result.put("role", user.role());
        result.put("status", user.status());
        result.put("lastAccess", user.lastAccess());
        return result;
    }

    public AppUser createBootstrapAdmin(String name, String email, String password) {
        Optional<AppUser> existing = repository.findByEmail(email);
        if (existing.isPresent()) {
            AppUser user = existing.get();
            if (!"admin".equals(user.role()) || !"Ativo".equals(user.status())) {
                throw new IllegalStateException(
                        "A conta configurada em AUTH_ADMIN_EMAIL existe, mas não é um administrador ativo."
                );
            }
            return user;
        }
        return repository.create(
                name.trim(),
                email,
                passwordEncoder.encode(requirePassword(password)),
                "admin",
                "Ativo"
        );
    }

    private void ensureEmailAvailable(String email, Long excludedUserId) {
        repository.findByEmail(email).ifPresent(user -> {
            if (excludedUserId == null || user.id() != excludedUserId) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com este e-mail.");
            }
        });
    }

    private void ensureLastAdminRemains(AppUser existing, String nextRole, String nextStatus) {
        boolean removingActiveAdmin = "admin".equals(existing.role())
                && "Ativo".equals(existing.status())
                && (!"admin".equals(nextRole) || !"Ativo".equals(nextStatus));
        if (removingActiveAdmin && repository.countActiveAdmins() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível desativar ou rebaixar o último administrador ativo.");
        }
    }

    private String requirePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha deve ter entre 8 e 72 caracteres.");
        }
        return password;
    }

    private String normalizeRole(String role) {
        if ("admin".equalsIgnoreCase(role)) return "admin";
        if ("user".equalsIgnoreCase(role)) return "user";
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Perfil de usuário inválido.");
    }

    private String normalizeStatus(String status) {
        if ("Ativo".equalsIgnoreCase(status)) return "Ativo";
        if ("Pendente".equalsIgnoreCase(status)) return "Pendente";
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status de usuário inválido.");
    }
}
