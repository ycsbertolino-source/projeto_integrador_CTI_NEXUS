package com.ctinexus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DashboardService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> obterDashboard(String loteId) {
        List<Map<String, Object>> lotes = loteId == null || loteId.isBlank()
                ? jdbcTemplate.queryForList("""
                    SELECT id, arquivo_nome, status, recebidas, invalidas, com_aviso, criado_em
                    FROM upload_lote
                    WHERE status = 'PROCESSADO'
                    ORDER BY criado_em DESC
                    FETCH FIRST 1 ROWS ONLY
                    """)
                : jdbcTemplate.queryForList("""
                    SELECT id, arquivo_nome, status, recebidas, invalidas, com_aviso, criado_em
                    FROM upload_lote
                    WHERE id = ?
                    """, loteId);

        if (lotes.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> lote = lotes.getFirst();
        String id = String.valueOf(lote.get("id"));
        List<String> resultados = jdbcTemplate.queryForList(
                "SELECT dados_json FROM estatistica_lote WHERE lote_id = ?",
                String.class, id
        );
        if (resultados.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> resultado = new LinkedHashMap<>(lote);
        resultado.put("loteId", id);
        resultado.put("estatisticas", lerJson(resultados.getFirst()));
        return resultado;
    }

    public List<Map<String, Object>> listarLotes() {
        return jdbcTemplate.queryForList("""
                SELECT id AS "loteId", arquivo_nome AS "arquivoNome", status,
                       recebidas, invalidas, com_aviso AS "comAviso", criado_em AS "criadoEm",
                       mensagem_erro AS "mensagemErro"
                FROM upload_lote
                ORDER BY criado_em DESC
                """);
    }

    private Map<String, Object> lerJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("As estatísticas persistidas estão inválidas.", error);
        }
    }
}
