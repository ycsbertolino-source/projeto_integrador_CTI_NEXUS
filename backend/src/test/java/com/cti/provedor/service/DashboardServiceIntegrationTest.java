package com.cti.provedor.service;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cti.provedor.config.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:dashboard;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.sql.init.mode=always",
    "app.jwt.secret=segredo-de-teste-com-mais-de-32-caracteres-ok"
})
@Transactional
class DashboardServiceIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void loadsPersistedDashboardAndUploadHistory() {
        jdbc.update("""
                INSERT INTO upload_lote (arquivo_nome, linhas_recebidas, linhas_validas, status)
                VALUES ('clientes.csv', 2, 2, 'PROCESSADO')
                """);
        Long loteId = jdbc.queryForObject("SELECT MAX(id) FROM upload_lote", Long.class);
        jdbc.update("""
                INSERT INTO dashboard_lote (upload_lote_id, dados_json)
                VALUES (?, '{"totalClientes":2,"resumo":{"total":1200}}')
                """, loteId);

        Map<String, Object> dashboard = dashboardService.obter(loteId);

        @SuppressWarnings("unchecked")
        Map<String, Object> lote = (Map<String, Object>) dashboard.get("lote");
        assertThat(lote)
                .containsEntry("id", loteId)
                .containsEntry("arquivoNome", "clientes.csv")
                .containsEntry("status", "PROCESSADO")
                .containsEntry("totalLinhas", 2);
        assertThat(dashboard.get("geradoEm")).isNotNull();
        JsonNode estatisticas = (JsonNode) dashboard.get("estatisticas");
        assertThat(estatisticas.get("totalClientes").asInt()).isEqualTo(2);
        assertThat(estatisticas.get("resumo").get("total").asInt()).isEqualTo(1200);

        // Sem loteId: devolve o último lote PROCESSADO
        assertThat(dashboardService.obter(null).get("lote")).isEqualTo(lote);

        var lotes = dashboardService.listarLotes();
        assertThat(lotes).hasSize(1);
        assertThat(lotes.getFirst()).containsEntry("id", loteId);
    }

    @Test
    void lotSentButNotYetCalculatedHasNullStatistics() {
        jdbc.update("INSERT INTO upload_lote (arquivo_nome, status) VALUES ('novo.csv', 'PROCESSANDO')");

        Map<String, Object> dashboard = dashboardService.obter(null);

        assertThat(dashboard.get("lote")).isNotNull();
        assertThat(dashboard.get("estatisticas")).isNull();
    }

    @Test
    void returns404WhenThereAreNoUploads() {
        assertThatThrownBy(() -> dashboardService.obter(null))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Nenhuma planilha");
        assertThatThrownBy(() -> dashboardService.obter(999L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Lote 999");
        assertThat(dashboardService.listarLotes()).isEmpty();
    }
}
