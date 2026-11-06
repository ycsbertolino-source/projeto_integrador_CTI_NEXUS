package com.cti.provedor.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Os testes dos slides 26 e 27 (Thunder Client), rodando sozinhos com "mvnw test". */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:teste;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok",
        "app.admin.senha="
})
@AutoConfigureMockMvc
class ClienteValidacaoControllerTest {

    @Autowired
    MockMvc mvc;

    private static final String URL = "/api/clientes/validar";

    @Test // Teste 1: JSON válido -> 200 com os dados padronizados
    void jsonValidoDevolveListaPreparada() throws Exception {
        String json = """
                [{
                  "codigoCliente": "CTI001",
                  "nomeCliente": "Metalúrgica Horizonte",
                  "consultor": "Ana Souza",
                  "segmento": "IND.",
                  "nivelCliente": "A",
                  "faturamentoAnual": 1850000,
                  "servicosContratados": "Internet Dedicada;Firewall",
                  "dataContratacao": "2025-01-15",
                  "cidade": "Campinas",
                  "uf": "SP"
                }]""";
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].segmento").value("Indústria"))
                .andExpect(jsonPath("$[0].dataContratacao").value("2025-01-15"))
                .andExpect(jsonPath("$[0].uf").value("SP"));
    }

    @Test // Teste 2: JSON inválido -> 400 {"mensagem": "Dados inválidos", "campo", "erro"}
    void jsonInvalidoDevolveErroClaro() throws Exception {
        String json = """
                [{
                  "codigoCliente": "CTI002",
                  "nomeCliente": "",
                  "consultor": "Carlos Lima",
                  "segmento": "Comercio",
                  "nivelCliente": "D",
                  "faturamentoAnual": -10,
                  "servicosContratados": "",
                  "dataContratacao": "2025-01-30",
                  "cidade": "Hortolândia",
                  "uf": "SPP"
                }]""";
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos"))
                .andExpect(jsonPath("$.campo").value("[0].nomeCliente")) // resposta esperada do slide 27
                .andExpect(jsonPath("$.erro").value("O nome do cliente é obrigatório"))
                .andExpect(jsonPath("$.campos['[0].nomeCliente']").value("O nome do cliente é obrigatório"))
                .andExpect(jsonPath("$.campos['[0].nivelCliente']").value("O nível deve ser A, B ou C"))
                .andExpect(jsonPath("$.campos['[0].faturamentoAnual']").value("O faturamento deve ser maior que zero"))
                .andExpect(jsonPath("$.campos['[0].servicosContratados']").value("Informe os serviços contratados"))
                .andExpect(jsonPath("$.campos['[0].uf']").value("A UF deve possuir duas letras"));
    }

    @Test // Service: código repetido no lote -> 400 "Regra de validação não atendida"
    void codigoRepetidoDevolveRegraDeNegocio() throws Exception {
        String linha = """
                {"codigoCliente": "CTI001", "nomeCliente": "X", "consultor": "Ana", "segmento": "Comercio",
                 "nivelCliente": "B", "faturamentoAnual": 10, "servicosContratados": "CRM",
                 "dataContratacao": "2025-01-15"}""";
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("[" + linha + "," + linha + "]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Regra de validação não atendida"))
                .andExpect(jsonPath("$.erro").value("Código repetido na planilha: CTI001"));
    }

    @Test // Data impossível -> 400 apontando o campo
    void dataInvalidaApontaOCampo() throws Exception {
        String json = """
                [{"codigoCliente": "CTI003", "nomeCliente": "X", "consultor": "Ana", "segmento": "Comercio",
                  "nivelCliente": "B", "faturamentoAnual": 10, "servicosContratados": "CRM",
                  "dataContratacao": "2025-02-30"}]""";
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("[0].dataContratacao"));
    }
}
