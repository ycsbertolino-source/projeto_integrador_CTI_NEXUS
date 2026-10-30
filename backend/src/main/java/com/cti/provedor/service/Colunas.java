package com.cti.provedor.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Os nomes das 10 colunas, para não escrever "codigo_cliente" espalhado pelo código. */
public final class Colunas {

    private Colunas() {}  // só usamos as constantes

    public static final String CODIGO = "codigo_cliente";
    public static final String NOME = "nome_cliente";
    public static final String CONSULTOR = "consultor";
    public static final String SEGMENTO = "segmento";
    public static final String NIVEL = "nivel_cliente";
    public static final String FATURAMENTO = "faturamento_anual";
    public static final String SERVICOS = "servicos_contratados";
    public static final String DATA = "data_contratacao";
    public static final String CIDADE = "cidade";
    public static final String UF = "uf";

    public static final List<String> TODAS =
            List.of(CODIGO, NOME, CONSULTOR, SEGMENTO, NIVEL, FATURAMENTO, SERVICOS, DATA, CIDADE, UF);

    public static final List<String> OBRIGATORIAS =  // cidade e uf ficam de fora
            List.of(CODIGO, NOME, CONSULTOR, SEGMENTO, NIVEL, FATURAMENTO, SERVICOS, DATA);

    public static final Map<String, Integer> TAMANHO_MAXIMO = Map.of(
            CODIGO, 60, NOME, 255, CONSULTOR, 120, SEGMENTO, 80, NIVEL, 20,
            SERVICOS, 400, CIDADE, 120, UF, 10);

    /** "Código do Cliente " -> "codigo_do_cliente" */
    public static String normalizarCabecalho(String texto) {
        if (texto == null) return "";
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcento.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
