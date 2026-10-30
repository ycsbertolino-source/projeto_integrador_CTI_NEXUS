package com.cti.provedor.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** "IND." vira Indústria. Serve para avisar o usuário; quem aplica de verdade é o Python. */
public final class Padronizacao {

    private Padronizacao() {}

    private static final Map<String, String> SEGMENTOS = Map.ofEntries(
            Map.entry("ind", "Indústria"), Map.entry("industria", "Indústria"),
            Map.entry("comercio", "Comércio"), Map.entry("com", "Comércio"),
            Map.entry("servico", "Serviços"), Map.entry("servicos", "Serviços"),
            Map.entry("serv", "Serviços"),
            Map.entry("saude", "Saúde"), Map.entry("educacao", "Educação"),
            Map.entry("tecnologia", "Tecnologia"), Map.entry("tec", "Tecnologia"),
            Map.entry("ti", "Tecnologia"));

    private static final Set<String> PARTICULAS = Set.of("de", "da", "do", "das", "dos", "e");

    /** "INDÚSTRIA" -> "industria", "IND." -> "ind" */
    static String simples(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }

    /** Nome oficial do segmento, ou null se não for reconhecido. */
    public static String segmento(String texto) { return SEGMENTOS.get(simples(texto)); }

    /** "ANA SOUZA" -> "Ana Souza"; "maria da silva" -> "Maria da Silva" */
    public static String tituloNome(String texto) {
        String[] palavras = texto.trim().toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder saida = new StringBuilder();
        for (int i = 0; i < palavras.length; i++) {
            String p = palavras[i];
            if (p.isEmpty()) continue;
            if (saida.length() > 0) saida.append(' ');
            if (i > 0 && PARTICULAS.contains(p)) saida.append(p);  // partículas minúsculas
            else saida.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return saida.toString();
    }
}
