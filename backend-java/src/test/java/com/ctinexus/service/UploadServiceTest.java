package com.ctinexus.service;

import com.ctinexus.dto.UploadRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadServiceTest {

    @Test
    void validatesRowsAgainAndSendsOnlyApprovedRowsToPython() {
        PythonEtlService python = mock(PythonEtlService.class);
        Map<String, Object> etlResponse = new HashMap<>();
        etlResponse.put("loteId", "lote-1");
        etlResponse.put("validas", 1);
        etlResponse.put("invalidas", 1);
        etlResponse.put("estatisticas", Map.of());
        when(python.enviarParaEtl(anyMap())).thenReturn(etlResponse);

        UploadRequest request = new UploadRequest();
        request.setArquivoNome("clientes.csv");
        request.setRecebidas(2);
        request.setInvalidas(0);
        request.setComAviso(0);
        request.setLinhas(List.of(
                Map.of(
                        "codigo_cliente", "CTI001",
                        "nome_cliente", "Cliente Um",
                        "consultor", "Ana Souza",
                        "segmento", "Indústria",
                        "nivel_cliente", "A",
                        "faturamento_anual", 1850000,
                        "servicos_contratados", "Firewall;Backup",
                        "data_contratacao", "2025-01-15"
                ),
                Map.of("codigo_cliente", "CTI002", "nivel_cliente", "Z")
        ));

        Map<String, Object> resultado = new UploadService(python).processarUpload(request);

        verify(python).enviarParaEtl(org.mockito.ArgumentMatchers.argThat(payload -> {
            List<?> linhas = (List<?>) payload.get("linhas");
            return linhas.size() == 1
                    && "CTI001".equals(((Map<?, ?>) linhas.getFirst()).get("codigo_cliente"))
                    && Integer.valueOf(1).equals(payload.get("invalidas"));
        }));
        assertEquals(1, ((List<?>) resultado.get("rejeitadas")).size());
    }

    @Test
    void rejectsInvalidDatesAndMoneyValues() {
        PythonEtlService python = mock(PythonEtlService.class);
        when(python.enviarParaEtl(anyMap())).thenAnswer(invocation -> {
            Map<String, Object> body = (Map<String, Object>) invocation.getArgument(0);
            return new HashMap<>(Map.of(
                    "loteId", body.get("loteId"),
                    "validas", 0,
                    "invalidas", 1,
                    "estatisticas", Map.of()
            ));
        });

        UploadRequest request = new UploadRequest();
        request.setRecebidas(1);
        request.setLinhas(List.of(Map.of(
                "codigo_cliente", "CTI003",
                "nome_cliente", "Cliente Três",
                "consultor", "João",
                "segmento", "Serviços",
                "nivel_cliente", "A",
                "faturamento_anual", "valor inválido",
                "servicos_contratados", "Suporte",
                "data_contratacao", "31/02/2025"
        )));

        Map<String, Object> resultado = new UploadService(python).processarUpload(request);
        assertEquals(1, ((List<?>) resultado.get("rejeitadas")).size());
    }
}
