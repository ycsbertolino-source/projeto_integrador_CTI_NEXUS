package com.cti.provedor.service;

import static org.assertj.core.api.Assertions.assertThat;

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
    "spring.sql.init.mode=always"
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

        Map<String, Object> dashboard = dashboardService.obterDashboard(loteId);

        assertThat(dashboard).containsEntry("loteId", loteId);
        assertThat(dashboard.get("estatisticas")).isEqualTo(
                Map.of("totalClientes", 2, "resumo", Map.of("total", 1200))
        );
        var lotes = dashboardService.listarLotes();
        assertThat(lotes).hasSize(1);
        assertThat(lotes.getFirst())
                .containsEntry("loteId", loteId)
                .containsEntry("arquivoNome", "clientes.csv");
    }

    @Test
    void returnsEmptyDashboardWhenThereAreNoProcessedUploads() {
        assertThat(dashboardService.obterDashboard(null)).isEmpty();
        assertThat(dashboardService.listarLotes()).isEmpty();
    }
}
