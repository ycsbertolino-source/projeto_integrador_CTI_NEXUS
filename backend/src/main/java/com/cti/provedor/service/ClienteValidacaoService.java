package com.cti.provedor.service;

import static com.cti.provedor.service.Colunas.CODIGO;
import static com.cti.provedor.service.Colunas.CONSULTOR;
import static com.cti.provedor.service.Colunas.DATA;
import static com.cti.provedor.service.Colunas.FATURAMENTO;
import static com.cti.provedor.service.Colunas.NIVEL;
import static com.cti.provedor.service.Colunas.NOME;
import static com.cti.provedor.service.Colunas.OBRIGATORIAS;
import static com.cti.provedor.service.Colunas.SEGMENTO;
import static com.cti.provedor.service.Colunas.SERVICOS;
import static com.cti.provedor.service.Colunas.TAMANHO_MAXIMO;
import static com.cti.provedor.service.Colunas.TODAS;
import static com.cti.provedor.service.Colunas.UF;

import com.cti.provedor.dto.ClienteDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/** Para cada linha: limpa o texto, confere obrigatórios, código e segmento. Erro bloqueia; aviso não. */
@Service
public class ClienteValidacaoService {

    public record ResultadoLinha(int numero, Map<String, String> valores,
                                 List<String> erros, List<String> avisos) {
        public boolean valida() { return erros.isEmpty(); }  // válida = nenhum erro
    }

    // ---- padrões e limites ----
    private static final Pattern RE_CODIGO = Pattern.compile("^[A-Za-z0-9_-]+$");
    private static final Pattern RE_UF = Pattern.compile("^[A-Za-z]{2}$");
    private static final Pattern RE_NUMERO = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Set<String> NIVEIS = Set.of("A", "B", "C");
    private static final BigDecimal UM_TRILHAO = new BigDecimal("1000000000000");
    private static final DateTimeFormatter DATA_BR =
            DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public List<ResultadoLinha> validar(List<ClienteDTO> linhas) {
        List<ResultadoLinha> saida = new ArrayList<>(linhas.size());
        Map<String, Integer> codigos = new HashMap<>();      // código -> 1ª linha onde apareceu
        Map<String, String> assinaturas = new HashMap<>();   // nome+faturamento+data -> código

        for (ClienteDTO linha : linhas) {
            Map<String, String> valores = new LinkedHashMap<>();
            List<String> erros = new ArrayList<>();
            List<String> avisos = new ArrayList<>();

            // 1) texto limpo + aviso se havia espaços extras; corta o que passa do tamanho
            for (String campo : TODAS) {
                String bruto = linha.campos().get(campo);
                String limpo = bruto == null ? "" : bruto.strip().replaceAll("\\s{2,}", " ");
                if (bruto != null && !limpo.equals(bruto)) avisos.add(campo + ": espaços extras removidos");
                Integer max = TAMANHO_MAXIMO.get(campo);
                if (max != null && limpo.length() > max) {
                    limpo = limpo.substring(0, max);
                    avisos.add(campo + ": texto cortado em " + max + " caracteres");
                }
                valores.put(campo, limpo);
            }

            // 2) obrigatórios
            for (String campo : OBRIGATORIAS)
                if (valores.get(campo).isEmpty()) erros.add(campo + ": campo obrigatório vazio");

            // 3) código: formato (letras, números, - e _) e duplicidade na planilha
            String codigo = valores.get(CODIGO);
            if (!codigo.isEmpty()) {
                if (!RE_CODIGO.matcher(codigo).matches())
                    erros.add(CODIGO + ": '" + codigo + "' inválido (use letras, números, - ou _)");
                Integer primeira = codigos.putIfAbsent(codigo.toUpperCase(Locale.ROOT), linha.numero());
                if (primeira != null)
                    erros.add(CODIGO + ": código duplicado (já usado na linha " + primeira + ")");
            }

            // 3b) segmento reconhecido (aviso se padronizado)
            String segmento = valores.get(SEGMENTO);
            if (!segmento.isEmpty()) {
                String oficial = Padronizacao.segmento(segmento);
                if (oficial == null) erros.add(SEGMENTO + ": '" + segmento + "' não reconhecido");
                else if (!oficial.equals(segmento))
                    avisos.add(SEGMENTO + ": '" + segmento + "' será padronizado para '" + oficial + "'");
            }

            // 3c) consultor: aviso se ajustado
            String consultor = valores.get(CONSULTOR);
            if (!consultor.isEmpty()) {
                String titulo = Padronizacao.tituloNome(consultor);
                if (!titulo.equals(consultor))
                    avisos.add(CONSULTOR + ": '" + consultor + "' será padronizado para '" + titulo + "'");
            }

            // 4) nível A/B/C (minúscula vira aviso)
            String nivel = valores.get(NIVEL);
            if (!nivel.isEmpty()) {
                String maiusculo = nivel.toUpperCase(Locale.ROOT);
                if (!NIVEIS.contains(maiusculo)) {
                    erros.add(NIVEL + ": '" + nivel + "' inválido (use A, B ou C)");
                } else if (!maiusculo.equals(nivel)) {
                    avisos.add(NIVEL + ": '" + nivel + "' convertido para '" + maiusculo + "'");
                    valores.put(NIVEL, maiusculo);
                }
            }

            // 5) faturamento: número, não negativo, abaixo de 1 trilhão; zero gera aviso
            String fat = valores.get(FATURAMENTO);
            if (!fat.isEmpty()) {
                Optional<BigDecimal> numero = lerDecimal(fat);  // aceita 1850000 · 1.850.000,50 · R$ ...
                if (numero.isEmpty()) erros.add(FATURAMENTO + ": '" + fat + "' não é um número");
                else if (numero.get().signum() < 0) erros.add(FATURAMENTO + ": não pode ser negativo");
                else if (numero.get().compareTo(UM_TRILHAO) >= 0) erros.add(FATURAMENTO + ": valor alto demais");
                else {
                    if (numero.get().signum() == 0) avisos.add(FATURAMENTO + ": valor igual a zero");
                    valores.put(FATURAMENTO, numero.get().setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            }

            // 6) serviços separados por ';' — pelo menos um
            String servicos = valores.get(SERVICOS);
            if (!servicos.isEmpty()) {
                List<String> lista = Arrays.stream(servicos.split(";")).map(String::strip)
                        .filter(s -> !s.isEmpty()).toList();
                if (lista.isEmpty()) erros.add(SERVICOS + ": informe pelo menos um serviço (separados por ';')");
                else valores.put(SERVICOS, String.join(";", lista));
            }

            // 7) data dd/mm/aaaa ou aaaa-mm-dd, a partir de 1990; no futuro gera aviso
            String data = valores.get(DATA);
            if (!data.isEmpty()) {
                Optional<LocalDate> d = lerData(data);
                if (d.isEmpty() || d.get().getYear() < 1990) {
                    erros.add(DATA + ": '" + data + "' não é uma data válida");
                } else {
                    if (d.get().isAfter(LocalDate.now())) avisos.add(DATA + ": data no futuro");
                    valores.put(DATA, d.get().toString());  // guarda como aaaa-mm-dd
                }
            }

            // 8) UF opcional, mas se vier precisa ter 2 letras
            String uf = valores.get(UF);
            if (!uf.isEmpty()) {
                if (!RE_UF.matcher(uf).matches()) erros.add(UF + ": '" + uf + "' inválida");
                else valores.put(UF, uf.toUpperCase(Locale.ROOT));
            }

            // 9) mesmo nome + faturamento + data com outro código: aviso de possível duplicidade
            if (erros.isEmpty()) {
                String chave = Padronizacao.simples(valores.get(NOME)) + "|" + valores.get(FATURAMENTO)
                        + "|" + valores.get(DATA);
                String outro = assinaturas.putIfAbsent(chave, codigo);
                if (outro != null && !outro.equalsIgnoreCase(codigo))
                    avisos.add(NOME + ": possível cliente duplicado (mesmos dados do código " + outro + ")");
            }

            saida.add(new ResultadoLinha(linha.numero(), valores, erros, avisos));
        }
        return saida;
    }

    /** Aceita 1850000 · 1850000.5 · 1.850.000,50 · R$ 1.850.000,50 */
    static Optional<BigDecimal> lerDecimal(String texto) {
        if (texto == null) return Optional.empty();
        String t = texto.replace("R$", "").replaceAll("[\\s\\u00A0]", "");
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");           // formato brasileiro
        } else if (t.indexOf('.') != t.lastIndexOf('.')) {
            t = t.replace(".", "");                             // 1.850.000 (só milhar)
        }
        if (!RE_NUMERO.matcher(t).matches()) return Optional.empty();
        return Optional.of(new BigDecimal(t));
    }

    /** Aceita dd/mm/aaaa (ou d/m/aaaa) e aaaa-mm-dd. */
    static Optional<LocalDate> lerData(String texto) {
        if (texto == null) return Optional.empty();
        String t = texto.strip();
        try {
            if (t.contains("/")) return Optional.of(LocalDate.parse(t, DATA_BR));
            return Optional.of(LocalDate.parse(t));             // ISO aaaa-mm-dd
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
