package com.cti.provedor.config;

import com.cti.provedor.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Cria o JWT no login e confere se um token é válido e não expirou. */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoSegundos;

    public JwtService(AppProperties props) {
        byte[] segredo = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (segredo.length < 32)
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres.");
        this.chave = Keys.hmacShaKeyFor(segredo);
        this.expiracaoSegundos = props.jwt().expirationMinutes() * 60;
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())                   // dono do token
                .claim("perfil", usuario.getPerfil().name())   // USER ou ADMIN
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(expiracaoSegundos)))
                .signWith(chave).compact();                   // ninguém consegue falsificar
    }

    public Optional<String> emailDoToken(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            return Optional.ofNullable(c.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();  // falso ou vencido
        }
    }

    public long getExpiracaoSegundos() { return expiracaoSegundos; }
}
