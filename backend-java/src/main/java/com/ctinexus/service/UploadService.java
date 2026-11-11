package com.ctinexus.service;

import com.ctinexus.dto.UploadRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private final PythonEtlService pythonEtlService;

    public UploadService(PythonEtlService pythonEtlService) {
        this.pythonEtlService = pythonEtlService;
    }

    public Map<String, Object> processarUpload(UploadRequest request) {
        List<Map<String, Object>> linhas = request.getLinhas() == null ? List.of() : request.getLinhas();
        List<Map<String, Object>> aprovadas = new ArrayList<>();
        List<Map<String, Object>> rejeitadas = new ArrayList<>();
        Set<String> codigos = new HashSet<>();

        for (int indice = 0; indice < linhas.size(); indice++) {
            if (linhas.get(indice) == null) {
                rejeitadas.add(Map.of("linha", indice + 2, "erros", List.of("registro vazio")));
                continue;
            }
            Map<String, Object> linha = new LinkedHashMap<>(linhas.get(indice));
            Object numeroLinha = linha.getOrDefault("numero_linha", indice + 2);
            List<String> erros = validar(linha);
            String codigo = texto(linha.get("codigo_cliente"));
            if (!codigo.isBlank() && !codigos.add(codigo.toUpperCase())) {
                erros.add("codigo_cliente duplicado neste lote");
            }

            if (erros.isEmpty()) {
                linha.put("codigo_cliente", codigo.toUpperCase());
                linha.put("consultor", texto(linha.get("consultor")));
                linha.put("segmento", texto(linha.get("segmento")));
                linha.put("nivel_cliente", texto(linha.get("nivel_cliente")).toUpperCase());
                linha.put("faturamento_anual", numero(linha.get("faturamento_anual")));
                linha.put("servicos_contratados", texto(linha.get("servicos_contratados")));
                linha.put("data_contratacao", data(linha.get("data_contratacao")).toString());
                aprovadas.add(linha);
            } else {
                rejeitadas.add(Map.of("linha", numeroLinha, "erros", erros));
            }
        }

        UUID loteId = UUID.randomUUID();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("loteId", loteId.toString());
        payload.put("arquivoNome", request.getArquivoNome() == null ? "planilha" : request.getArquivoNome());
        payload.put("recebidas", Math.max(request.getRecebidas(), linhas.size() + request.getInvalidas()));
        payload.put("invalidas", request.getInvalidas() + rejeitadas.size());
        payload.put("comAviso", request.getComAviso());
        payload.put("linhas", aprovadas);

        Map<String, Object> resultado = pythonEtlService.enviarParaEtl(payload);
        resultado.put("rejeitadas", rejeitadas);
        resultado.put("validacaoJava", Map.of(
                "aprovadas", aprovadas.size(),
                "rejeitadas", rejeitadas.size()
        ));
        return resultado;
    }

    private List<String> validar(Map<String, Object> linha) {
        List<String> erros = new ArrayList<>();
        String codigo = texto(linha.get("codigo_cliente"));
        if (codigo.isBlank()) erros.add("codigo_cliente obrigatório");
        if (texto(linha.get("nome_cliente")).isBlank()) erros.add("nome_cliente obrigatório");
        if (texto(linha.get("consultor")).isBlank()) erros.add("consultor obrigatório");
        if (texto(linha.get("segmento")).isBlank()) erros.add("segmento obrigatório");

        String nivel = texto(linha.get("nivel_cliente")).toUpperCase();
        if (!Set.of("A", "B", "C").contains(nivel)) erros.add("nivel_cliente deve ser A, B ou C");

        try {
            if (numero(linha.get("faturamento_anual")).compareTo(BigDecimal.ZERO) < 0) {
                erros.add("faturamento_anual deve ser não negativo");
            }
        } catch (NumberFormatException error) {
            erros.add("faturamento_anual inválido");
        }

        if (texto(linha.get("servicos_contratados")).isBlank()) {
            erros.add("servicos_contratados obrigatório");
        }
        try {
            data(linha.get("data_contratacao"));
        } catch (DateTimeException error) {
            erros.add("data_contratacao inválida");
        }
        return erros;
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor).trim();
    }

    private BigDecimal numero(Object valor) {
        if (valor instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        String texto = texto(valor).replace("R$", "").replace(" ", "");
        if (texto.contains(",")) {
            texto = texto.replace(".", "").replace(",", ".");
        }
        return new BigDecimal(texto);
    }

    private LocalDate data(Object valor) {
        String texto = texto(valor);
        if (texto.length() >= 10 && texto.charAt(4) == '-') {
            return LocalDate.parse(texto.substring(0, 10));
        }
        return LocalDate.parse(
                texto,
                DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)
        );
    }
}
