package com.ctinexus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PythonEtlService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${etl.api.url:http://localhost:8000/etl}")
    private String etlApiUrl;

    @Value("${etl.api.key:}")
    private String etlApiKey;

    public Map<String, Object> enviarParaEtl(Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (etlApiKey != null && !etlApiKey.isBlank()) {
            headers.set("X-ETL-Key", etlApiKey);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(etlApiUrl, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Falha ao processar ETL no Python.");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> resultado = response.getBody();
        return resultado;
    }
}
