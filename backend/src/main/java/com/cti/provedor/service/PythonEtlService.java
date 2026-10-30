package com.cti.provedor.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.config.AppProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Faz POST /etl com as linhas válidas. O Java não grava nada do upload. */
@Component
public class PythonEtlService {

    private static final Logger log = LoggerFactory.getLogger(PythonEtlService.class);

    private final String url;
    private final String chave;
    private final HttpClient http;
    private final ObjectMapper json;

    public PythonEtlService(AppProperties props, ObjectMapper json) {
        this.url = props.etl().url();
        this.chave = props.etl().key() == null ? "" : props.etl().key().trim();
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.json = json;
    }

    public void enviar(String arquivoNome, int recebidas, int invalidas, int comAviso,
                       List<Map<String, Object>> linhas) {
        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("arquivoNome", arquivoNome);
        pedido.put("recebidas", recebidas);
        pedido.put("invalidas", invalidas);
        pedido.put("comAviso", comAviso);
        pedido.put("linhas", linhas);

        byte[] corpo;
        try {
            corpo = json.writeValueAsBytes(pedido);
        } catch (JsonProcessingException e) {
            throw new ApiException(INTERNAL_SERVER_ERROR, "Não foi possível montar o envio ao serviço Python.");
        }

        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(corpo));
        chamar(req);
    }

    private void chamar(HttpRequest.Builder req) {
        if (!chave.isEmpty()) req.header("X-ETL-Key", chave);
        HttpResponse<String> resp;
        try {
            resp = http.send(req.build(), HttpResponse.BodyHandlers.ofString(UTF_8));
        } catch (IOException e) {  // Python desligado
            log.warn("Falha ao chamar o Python em {}: {}", url, e.getMessage());
            throw new ApiException(SERVICE_UNAVAILABLE, "Não foi possível falar com o serviço Python. Ele está ligado?");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(SERVICE_UNAVAILABLE, "O envio ao serviço Python foi interrompido.");
        }
        if (resp.statusCode() != 200) {
            log.warn("Python respondeu {}: {}", resp.statusCode(), resp.body());
            throw new ApiException(BAD_GATEWAY, "O serviço Python respondeu com erro (HTTP " + resp.statusCode() + ").");
        }
    }
}
