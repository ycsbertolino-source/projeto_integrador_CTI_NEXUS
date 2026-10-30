package com.cti.provedor.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

/** Em toda requisição lê "Authorization: Bearer ..." e diz ao Spring quem é o usuário. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService detailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService detailsService) {
        this.jwtService = jwtService;
        this.detailsService = detailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = cabecalho.substring(7).trim();  // tira o "Bearer "
            jwtService.emailDoToken(token).ifPresent(email -> {
                try {
                    UserDetails usuario = detailsService.loadUserByUsername(email);
                    if (usuario.isEnabled()) {  // bloqueado ou pendente não passa
                        var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (UsernameNotFoundException e) {
                    // segue sem autenticar, vai dar 401
                }
            });
        }
        chain.doFilter(request, response);  // passa para o próximo filtro / a rota
    }
}
