package com.ctinexus.config;

import com.ctinexus.service.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootstrapAdminConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapAdminConfiguration.class);

    @Bean
    ApplicationRunner bootstrapAdmin(
            AppUserService appUserService,
            @Value("${auth.bootstrap.email:}") String email,
            @Value("${auth.bootstrap.password:}") String password,
            @Value("${auth.bootstrap.name:Administrador}") String name
    ) {
        return args -> {
            if (email.isBlank() && password.isBlank()) {
                logger.warn("Administrador inicial não configurado. Defina AUTH_ADMIN_EMAIL e AUTH_ADMIN_PASSWORD.");
                return;
            }
            if (email.isBlank() || password.isBlank() || password.length() < 12 || password.length() > 72) {
                throw new IllegalStateException(
                        "Configure AUTH_ADMIN_EMAIL e uma AUTH_ADMIN_PASSWORD com 12 a 72 caracteres."
                );
            }
            appUserService.createBootstrapAdmin(name, email, password);
        };
    }
}
