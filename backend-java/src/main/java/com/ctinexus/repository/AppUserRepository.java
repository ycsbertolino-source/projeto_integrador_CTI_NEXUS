package com.ctinexus.repository;

import com.ctinexus.model.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Repository
public class AppUserRepository {

    private static final DateTimeFormatter LAST_ACCESS_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AppUser> rowMapper = (resultSet, rowNumber) -> {
        Timestamp lastAccess = resultSet.getTimestamp("ultimo_acesso");
        return new AppUser(
                resultSet.getLong("id"),
                resultSet.getString("nome"),
                resultSet.getString("email"),
                resultSet.getString("senha_hash"),
                resultSet.getString("perfil"),
                resultSet.getString("status"),
                lastAccess == null ? "Nunca" : lastAccess.toLocalDateTime().format(LAST_ACCESS_FORMAT),
                resultSet.getInt("token_version")
        );
    };

    public AppUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AppUser> findByEmail(String email) {
        return jdbcTemplate.query(
                "SELECT * FROM app_user WHERE email = ?",
                rowMapper,
                normalizeEmail(email)
        ).stream().findFirst();
    }

    public Optional<AppUser> findById(long id) {
        return jdbcTemplate.query(
                "SELECT * FROM app_user WHERE id = ?",
                rowMapper,
                id
        ).stream().findFirst();
    }

    public List<AppUser> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM app_user ORDER BY nome, id",
                rowMapper
        );
    }

    public AppUser create(String name, String email, String passwordHash, String role, String status) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> createUserStatement(
                connection, name, email, passwordHash, role, status
        ), keyHolder);
        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return findById(id).orElseThrow();
    }

    public AppUser update(long id, String name, String email, String passwordHash, String role, String status) {
        if (passwordHash == null) {
            jdbcTemplate.update("""
                    UPDATE app_user
                    SET nome = ?, email = ?, perfil = ?, status = ?
                    WHERE id = ?
                    """, name, normalizeEmail(email), role, status, id);
        } else {
            jdbcTemplate.update("""
                    UPDATE app_user
                    SET nome = ?, email = ?, senha_hash = ?, perfil = ?, status = ?,
                        token_version = token_version + 1
                    WHERE id = ?
                    """, name, normalizeEmail(email), passwordHash, role, status, id);
        }
        return findById(id).orElseThrow();
    }

    public void updateProfile(long id, String name, String email, String passwordHash) {
        if (passwordHash == null) {
            jdbcTemplate.update(
                    "UPDATE app_user SET nome = ?, email = ? WHERE id = ?",
                    name, normalizeEmail(email), id
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE app_user SET nome = ?, email = ?, senha_hash = ?, token_version = token_version + 1 WHERE id = ?",
                    name, normalizeEmail(email), passwordHash, id
            );
        }
    }

    public void markSuccessfulLogin(long id) {
        jdbcTemplate.update(
                "UPDATE app_user SET ultimo_acesso = CURRENT_TIMESTAMP WHERE id = ?",
                id
        );
    }

    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM app_user WHERE id = ?", id);
    }

    public long countActiveAdmins() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE perfil = 'admin' AND status = 'Ativo'",
                Long.class
        );
        return count == null ? 0 : count;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private PreparedStatement createUserStatement(
            Connection connection,
            String name,
            String email,
            String passwordHash,
            String role,
            String status
    ) throws java.sql.SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO app_user (nome, email, senha_hash, perfil, status)
                VALUES (?, ?, ?, ?, ?)
                """, new String[]{"id"});
        statement.setString(1, name);
        statement.setString(2, normalizeEmail(email));
        statement.setString(3, passwordHash);
        statement.setString(4, role);
        statement.setString(5, status);
        return statement;
    }
}
