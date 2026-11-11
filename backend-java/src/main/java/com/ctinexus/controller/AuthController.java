package com.ctinexus.controller;

import com.ctinexus.dto.LoginRequest;
import com.ctinexus.dto.ProfileUpdateRequest;
import com.ctinexus.dto.RegisterRequest;
import com.ctinexus.model.AppUser;
import com.ctinexus.security.JwtService;
import com.ctinexus.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;
    private final JwtService jwtService;

    public AuthController(AppUserService appUserService, JwtService jwtService) {
        this.appUserService = appUserService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request) {
        AppUser user = appUserService.authenticate(request.email(), request.password());
        return Map.of(
                "token", jwtService.issueToken(user.email(), user.tokenVersion()),
                "usuario", appUserService.publicUser(user)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = appUserService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Cadastro recebido. Aguarde a aprovação de um administrador.",
                "usuario", appUserService.publicUser(user)
        ));
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(@AuthenticationPrincipal AppUser user) {
        return appUserService.publicUser(user);
    }

    @PutMapping("/me")
    public Map<String, Object> updateProfile(
            @AuthenticationPrincipal AppUser user,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        AppUser updatedUser = appUserService.updateOwnProfile(user, request);
        return Map.of(
                "token", jwtService.issueToken(updatedUser.email(), updatedUser.tokenVersion()),
                "usuario", appUserService.publicUser(updatedUser)
        );
    }
}
