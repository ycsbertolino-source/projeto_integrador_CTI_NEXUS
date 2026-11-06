package com.cti.provedor.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Trocar a própria senha e o admin redefinir a senha de quem esqueceu. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:senha;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok",
        "app.admin.email=admin@teste.local",
        "app.admin.senha=Admin12345"
})
@AutoConfigureMockMvc
class SenhaTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String login(String email, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return "Bearer " + json.readTree(corpo).get("token").asText();
    }

    @Test
    void usuarioTrocaASenhaEAdminRedefineDeOutro() throws Exception {
        String admin = login("admin@teste.local", "Admin12345");

        // senha atual errada -> 400
        mvc.perform(patch("/api/auth/senha").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"errada123\",\"novaSenha\":\"NovaSenha1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A senha atual está incorreta."));

        // nova senha fraca -> 400 com o campo
        mvc.perform(patch("/api/auth/senha").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"Admin12345\",\"novaSenha\":\"curta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("novaSenha"));

        // troca certa -> 200, e o login passa a usar a senha nova
        mvc.perform(patch("/api/auth/senha").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"Admin12345\",\"novaSenha\":\"NovaSenha1\"}"))
                .andExpect(status().isOk());
        admin = login("admin@teste.local", "NovaSenha1");

        // cadastra um usuário, admin aprova e redefine a senha dele
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Bia\",\"email\":\"bia@teste.local\",\"senha\":\"Original1\"}"))
                .andExpect(status().isCreated());
        JsonNode lista = json.readTree(mvc.perform(get("/api/admin/usuarios").header("Authorization", admin))
                .andReturn().getResponse().getContentAsString());
        long idBia = 0;
        for (JsonNode u : lista) if (u.get("email").asText().equals("bia@teste.local")) idBia = u.get("id").asLong();

        mvc.perform(patch("/api/admin/usuarios/" + idBia + "/status").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/api/admin/usuarios/" + idBia + "/senha").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"novaSenha\":\"Redefinida1\"}"))
                .andExpect(status().isOk());
        login("bia@teste.local", "Redefinida1");

        // usuário comum não pode redefinir senha de ninguém -> 403
        String bia = login("bia@teste.local", "Redefinida1");
        mvc.perform(patch("/api/admin/usuarios/1/senha").header("Authorization", bia)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"novaSenha\":\"Qualquer1\"}"))
                .andExpect(status().isForbidden());
    }
}
