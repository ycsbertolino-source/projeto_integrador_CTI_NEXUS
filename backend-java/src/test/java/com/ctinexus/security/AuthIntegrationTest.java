package com.ctinexus.security;

import com.ctinexus.service.AppUserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@test.local";
    private static final String ADMIN_PASSWORD = "StrongTestPassword123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AppUserService appUserService;

    @BeforeEach
    void resetUsers() {
        jdbcTemplate.update("DELETE FROM app_user");
        appUserService.createBootstrapAdmin("Test Admin", ADMIN_EMAIL, ADMIN_PASSWORD);
    }

    @Test
    void protectedApiRejectsMissingAndInvalidBearerTokens() throws Exception {
        mockMvc.perform(get("/api/lotes"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/lotes").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationWaitsForAdminApprovalBeforeLogin() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Usuária Nova",
                                  "email":"nova@example.com",
                                  "password":"SecurePass123",
                                  "role":"admin",
                                  "status":"Ativo"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode registeredUser = objectMapper.readTree(registration.getResponse().getContentAsString())
                .path("usuario");
        assertThat(registeredUser.path("role").asText()).isEqualTo("user");
        assertThat(registeredUser.path("status").asText()).isEqualTo("Pendente");
        assertThat(registeredUser.has("passwordHash")).isFalse();
        String passwordHash = jdbcTemplate.queryForObject(
                "SELECT senha_hash FROM app_user WHERE email = ?",
                String.class,
                "nova@example.com"
        );
        assertThat(passwordHash).startsWith("$2a$");
        assertThat(passwordHash).isNotEqualTo("SecurePass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nova@example.com","password":"SecurePass123"}
                                """))
                .andExpect(status().isForbidden());

        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginPayload(ADMIN_EMAIL, ADMIN_PASSWORD)
                        )))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString())
                .path("token").asText();
        assertThat(adminToken).isNotBlank();

        String usersJson = mockMvc.perform(get("/api/users").header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode users = objectMapper.readTree(usersJson);
        assertThat(users.findValues("email").stream().map(JsonNode::asText))
                .contains("nova@example.com");
    }

    @Test
    void adminCanApproveUserAndJwtGrantsProtectedApiAccess() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Usuária Nova","email":"nova@example.com","password":"SecurePass123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long userId = objectMapper.readTree(registration.getResponse().getContentAsString())
                .path("usuario").path("id").asLong();

        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(put("/api/users/{id}", userId)
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Usuária Nova","email":"nova@example.com","role":"user","status":"Ativo"}
                                """))
                .andExpect(status().isOk());

        String userToken = login("nova@example.com", "SecurePass123");
        mockMvc.perform(get("/api/lotes").header("Authorization", bearer(userToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users").header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changingPasswordRevokesOldTokenAndReturnsANewToken() throws Exception {
        String oldToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
        MvcResult updateResult = mockMvc.perform(put("/api/auth/me")
                        .header("Authorization", bearer(oldToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Test Admin",
                                  "email":"admin@test.local",
                                  "currentPassword":"StrongTestPassword123!",
                                  "newPassword":"NewSecurePassword456!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String newToken = objectMapper.readTree(updateResult.getResponse().getContentAsString())
                .path("token").asText();

        mockMvc.perform(get("/api/lotes").header("Authorization", bearer(oldToken)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/lotes").header("Authorization", bearer(newToken)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@test.local","password":"NewSecurePassword456!"}
                                """))
                .andExpect(status().isOk());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record LoginPayload(String email, String password) {
    }
}
