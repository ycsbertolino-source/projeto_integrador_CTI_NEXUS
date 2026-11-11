package com.ctinexus.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardServiceTest {

    private JdbcTemplate jdbcTemplate;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:dashboard;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        dataSource.setDriverClassName("org.h2.Driver");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS upload_lote (
                    id VARCHAR(36) PRIMARY KEY,
                    arquivo_nome VARCHAR(255),
                    status VARCHAR(20),
                    recebidas INT,
                    invalidas INT,
                    com_aviso INT,
                    mensagem_erro VARCHAR(255),
                    criado_em TIMESTAMP
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS estatistica_lote (
                    lote_id VARCHAR(36) PRIMARY KEY,
                    dados_json CLOB
                )
                """);
        jdbcTemplate.update("DELETE FROM estatistica_lote");
        jdbcTemplate.update("DELETE FROM upload_lote");
        dashboardService = new DashboardService(jdbcTemplate, new ObjectMapper());
    }

    @Test
    void returnsStatisticsForTheLatestProcessedLot() {
        jdbcTemplate.update("""
                INSERT INTO upload_lote (id, arquivo_nome, status, recebidas, invalidas, com_aviso, criado_em)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """, "lote-1", "clientes.csv", "PROCESSADO", 2, 0, 0);
        jdbcTemplate.update(
                "INSERT INTO estatistica_lote (lote_id, dados_json) VALUES (?, ?)",
                "lote-1", "{\"totalClientes\":2,\"resumo\":{\"media\":100.0}}"
        );

        Map<String, Object> dashboard = dashboardService.obterDashboard(null);
        Map<?, ?> estatisticas = (Map<?, ?>) dashboard.get("estatisticas");
        Map<?, ?> resumo = (Map<?, ?>) estatisticas.get("resumo");

        assertEquals("lote-1", dashboard.get("loteId"));
        assertEquals(2, estatisticas.get("totalClientes"));
        assertEquals(100.0, resumo.get("media"));
    }
}
