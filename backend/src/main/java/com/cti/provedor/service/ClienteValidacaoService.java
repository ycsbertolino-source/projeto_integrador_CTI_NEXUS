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
import com.cti.provedor.dto.LinhaPlanilha;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

/**
 * Validações que dependem do lote: o DTO valida uma linha, o Service olha a lista inteira.
 * - validarEPreparar: rota da aula POST /api/clientes/validar (duplicidade + padronização).
 * - validar: usado pelo upload completo (/api/uploads/validadas), que depois segue para o Python.
 */
@Service // o Spring cria esta classe automaticamente
public class ClienteValidacaoService { // classe de regras complementares

    // =====================================================================
    // ETAPA DA AULA: DTO já validado pelo @Valid -> verifica lote e padroniza
    // =====================================================================

    public List<ClienteDTO> validarEPreparar(List<ClienteDTO> clientes) { // recebe lista do controller
        if (clientes == null || clientes.isEmpty()) { // lista vazia não tem o que validar
            throw new IllegalArgumentException("Envie pelo menos um cliente para validar"); // erro claro
        } // fim do if

        Set<String> codigos = new HashSet<>(); // guarda códigos já encontrados

        for (ClienteDTO cliente : clientes) { // percorre cada cliente recebido
            String codigo = cliente.codigoCliente().trim().toUpperCase(Locale.ROOT); // remove espaços (CTI001 = cti001)
            if (!codigos.add(codigo)) { // se já existe, é repetido
                throw new IllegalArgumentException("Código repetido na planilha: " + codigo); // erro claro
            } // fim do if
        } // fim do for

        return clientes.stream() // transforma a lista em fluxo de processamento
                .map(this::padronizar) // aplica padronização em cada cliente
                .toList(); // volta para lista
    } // fim do método validarEPreparar

    private ClienteDTO padronizar(ClienteDTO c) { // método privado para limpar uma linha
        return new ClienteDTO( // cria um novo DTO já preparado
                c.codigoCliente().trim().toUpperCase(Locale.ROOT), // remove espaços do código
                c.nomeCliente().trim(), // remove espaços do nome
                formatarNome(c.consultor()), // deixa consultor mais legível
                padronizarSegmento(c.segmento()), // normaliza o segmento
                c.nivelCliente().trim().toUpperCase(Locale.ROOT), // nível sempre em maiúsculo
                c.faturamentoAnual(), // mantém valor numérico validado
                c.servicosContratados().trim(), // remove espaços dos serviços
                c.dataContratacao(), // mantém data validada
                c.cidade() == null ? "" : c.cidade().trim(), // cidade opcional
                c.uf() == null ? "" : c.uf().trim().toUpperCase(Locale.ROOT) // UF opcional em maiúsculo
        ); // fim do novo DTO
    } // fim do método padronizar

    private String padronizarSegmento(String segmento) { // recebe segmento da planilha
        String s = segmento.trim().toLowerCase(Locale.ROOT); // remove espaços e deixa minúsculo
        if (s.equals("ind.") || s.equals("industria") || s.equals("indústria")) { // variações de indústria
            return "Indústria"; // retorna padrão único
        } // fim do if
        if (s.equals("comercio") || s.equals("comércio")) { // variações de comércio
            return "Comércio"; // retorna padrão único
        } // fim do if
        if (s.equals("servicos") || s.equals("serviços")) { // variações de serviços
            return "Serviços"; // retorna padrão único
        } // fim do if
        String oficial = Padronizacao.segmento(segmento); // outras variações (Saúde, Educação, TI...)
        return oficial != null ? oficial : segmento.trim(); // se não conhece, mantém texto sem espaços extras
    } // fim do método

    private String formatarNome(String nome) { // recebe nome do consultor
        // "ANA SOUZA" -> "Ana Souza" (cada palavra com a primeira letra maiúscula; "da", "de" ficam minúsculos)
        return Padronizacao.tituloNome(nome); // remove espaços e ajusta maiúsculas
    } // fim do método

    // =====================================================================
    // UPLOAD COMPLETO (rota /api/uploads/validadas): lista erros e avisos por linha
    // =====================================================================

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

    public List<ResultadoLinha> validar(List<LinhaPlanilha> linhas) {
        List<ResultadoLinha> saida = new ArrayList<>(linhas.size());
        Map<String, Integer> codigos = new HashMap<>();      // código -> 1ª linha onde apareceu
        Map<String, String> assinaturas = new HashMap<>();   // nome+faturamento+data -> código

        for (LinhaPlanilha linha : linhas) {
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

            // 5) faturamento: número maior que zero (mesma regra do @Positive do DTO) e abaixo de 1 trilhão
            String fat = valores.get(FATURAMENTO);
            if (!fat.isEmpty()) {
                Optional<BigDecimal> numero = lerDecimal(fat);  // aceita 1850000 · 1.850.000,50 · R$ ...
                if (numero.isEmpty()) erros.add(FATURAMENTO + ": '" + fat + "' não é um número");
                else if (numero.get().signum() <= 0) erros.add(FATURAMENTO + ": deve ser maior que zero");
                else if (numero.get().compareTo(UM_TRILHAO) >= 0) erros.add(FATURAMENTO + ": valor alto demais");
                else {
                    valores.put(FATURAMENTO, numero.get().setScale(2, RoundingMode.HALF_UP).toPlainString());
                }
            }

            // 6) serviços separados por ';' ou ',' (a planilha modelo usa vírgula) — pelo menos um
            String servicos = valores.get(SERVICOS);
            if (!servicos.isEmpty()) {
                List<String> lista = Arrays.stream(servicos.split("[;,]")).map(String::strip)
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
