package com.ctinexus.security;

import com.ctinexus.model.AppUser;
import com.ctinexus.service.AppUserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserService appUserService;

    public JwtAuthenticationFilter(JwtService jwtService, AppUserService appUserService) {
        this.jwtService = jwtService;
        this.appUserService = appUserService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                String email = jwtService.extractEmail(token);
                int tokenVersion = jwtService.extractTokenVersion(token);
                appUserService.findActiveByEmail(email)
                        .filter(user -> user.tokenVersion() == tokenVersion)
                        .ifPresent(this::authenticate);
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(AppUser user) {
        var authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role().toUpperCase()))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
