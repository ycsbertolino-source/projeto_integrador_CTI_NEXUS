package com.cti.provedor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public DashboardService(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public Map<String, Object> obterDashboard(Long loteId) {
        List<Map<String, Object>> lotes = loteId == null
                ? jdbc.queryForList("""
                    SELECT id, arquivo_nome, status, linhas_recebidas, linhas_validas,
                           linhas_invalidas, linhas_com_aviso, criado_em
                    FROM upload_lote
                    WHERE status = 'PROCESSADO'
                    ORDER BY criado_em DESC
                    FETCH FIRST 1 ROWS ONLY
                    """)
                : jdbc.queryForList("""
                    SELECT id, arquivo_nome, status, linhas_recebidas, linhas_validas,
                           linhas_invalidas, linhas_com_aviso, criado_em
                    FROM upload_lote WHERE id = ?
                    """, loteId);
        if (lotes.isEmpty()) return Map.of();

        Map<String, Object> lote = lotes.getFirst();
        Long id = ((Number) lote.get("id")).longValue();
        List<String> dados = jdbc.queryForList(
                "SELECT dados_json FROM dashboard_lote WHERE upload_lote_id = ?",
                String.class,
                id
        );
        if (dados.isEmpty()) return Map.of();

        Map<String, Object> resultado = new LinkedHashMap<>(lote);
        resultado.put("loteId", id);
        try {
            resultado.put("estatisticas", json.readValue(dados.getFirst(), new TypeReference<>() {}));
        } catch (JsonProcessingException error) {
            throw new IllegalStateException("As estatísticas persistidas estão inválidas.", error);
        }
        return resultado;
    }

    public List<Map<String, Object>> listarLotes() {
        return jdbc.queryForList("""
                SELECT id AS "loteId", arquivo_nome AS "arquivoNome", status,
                       linhas_recebidas AS "linhasRecebidas", linhas_validas AS "linhasValidas",
                       linhas_invalidas AS "linhasInvalidas", linhas_com_aviso AS "linhasComAviso",
                       criado_em AS "criadoEm", mensagem_erro AS "mensagemErro"
                FROM upload_lote
                ORDER BY criado_em DESC
                """);
    }
}
