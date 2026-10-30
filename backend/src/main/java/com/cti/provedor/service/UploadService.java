package com.cti.provedor.service;

import static com.cti.provedor.service.Colunas.CIDADE;
import static com.cti.provedor.service.Colunas.CODIGO;
import static com.cti.provedor.service.Colunas.CONSULTOR;
import static com.cti.provedor.service.Colunas.DATA;
import static com.cti.provedor.service.Colunas.FATURAMENTO;
import static com.cti.provedor.service.Colunas.NIVEL;
import static com.cti.provedor.service.Colunas.NOME;
import static com.cti.provedor.service.Colunas.SEGMENTO;
import static com.cti.provedor.service.Colunas.SERVICOS;
import static com.cti.provedor.service.Colunas.TODAS;
import static com.cti.provedor.service.Colunas.UF;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.config.AppProperties;
import com.cti.provedor.dto.ClienteDTO;
import com.cti.provedor.dto.UploadDtos.EnvioValidadasRequest;
import com.cti.provedor.dto.UploadDtos.LinhaEnvio;
import com.cti.provedor.service.ClienteValidacaoService.ResultadoLinha;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** O cérebro do upload: confere o pedido, valida de novo e manda só as válidas ao Python. */
@Service
public class UploadService {

    private final ClienteValidacaoService validacao;
    private final AppProperties props;
    private final PythonEtlService python;

    public UploadService(ClienteValidacaoService validacao, AppProperties props, PythonEtlService python) {
        this.validacao = validacao;
        this.props = props;
        this.python = python;
    }

    public Map<String, Object> receberValidadas(EnvioValidadasRequest req) {
        if (req == null || req.linhas() == null || req.linhas().isEmpty())  // sem linhas: 400
            throw new ApiException(HttpStatus.BAD_REQUEST, "Não há linhas validadas para enviar.");
        if (req.linhas().size() > props.upload().maxLinhas())             // acima do limite: 422
            throw new ApiException(UNPROCESSABLE_ENTITY,
                    "A planilha passou do limite de " + props.upload().maxLinhas() + " linhas.");
        String nome = nomeSeguro(req.arquivoNome());
        int descartadas = req.descartadas() == null ? 0 : Math.max(0, req.descartadas());

        List<ClienteDTO> brutas = new ArrayList<>(req.linhas().size());
        int posicao = 2;  // a linha 1 da planilha é o cabeçalho
        for (LinhaEnvio e : req.linhas()) {
            if (e == null) { posicao++; continue; }
            Map<String, String> campos = new LinkedHashMap<>();
            campos.put(CODIGO, e.codigoCliente());
            campos.put(NOME, e.nomeCliente());
            campos.put(CONSULTOR, e.consultor());
            campos.put(SEGMENTO, e.segmento());
            campos.put(NIVEL, e.nivelCliente());
            campos.put(FATURAMENTO, e.faturamentoAnual());
            campos.put(SERVICOS, e.servicosContratados());
            campos.put(DATA, e.dataContratacao());
            campos.put(CIDADE, e.cidade());
            campos.put(UF, e.uf());
            int numero = e.numeroLinha() == null ? posicao : e.numeroLinha();
            brutas.add(new ClienteDTO(numero, campos));
            posicao++;
        }
        return registrar(nome, brutas, descartadas);  // valida de novo e envia ao Python
    }

    private Map<String, Object> registrar(String nome, List<ClienteDTO> brutas, int descartadas) {
        List<ResultadoLinha> resultado = validacao.validar(brutas);  // VALIDAÇÃO NO BACKEND
        List<Map<String, Object>> validas = new ArrayList<>();
        List<String> rejeitadas = new ArrayList<>();
        int comAviso = 0;
        for (ResultadoLinha r : resultado) {
            if (!r.valida()) {  // linha com erro: só informa o usuário
                rejeitadas.add("Linha " + r.numero() + ": " + String.join(" | ", r.erros()));
                continue;
            }
            if (!r.avisos().isEmpty()) comAviso++;
            validas.add(paraPython(r));
        }
        if (validas.isEmpty())
            throw new ApiException(UNPROCESSABLE_ENTITY, "Nenhuma linha válida para enviar. Corrija a planilha e tente de novo.");

        int recebidas = resultado.size() + descartadas;
        int invalidas = rejeitadas.size() + descartadas;
        python.enviar(nome, recebidas, invalidas, comAviso, validas);  // o Python grava

        Map<String, Object> saida = new LinkedHashMap<>();
        saida.put("status", "PROCESSADO");
        saida.put("arquivoNome", nome);
        saida.put("totalLinhas", recebidas);
        saida.put("linhasValidas", validas.size());
        saida.put("linhasInvalidas", invalidas);
        saida.put("linhasComAviso", comAviso);
        saida.put("rejeitadas", rejeitadas);
        return saida;
    }

    /** Uma linha válida no formato que o Python recebe (coluna -> valor). */
    private Map<String, Object> paraPython(ResultadoLinha r) {
        Map<String, Object> linha = new LinkedHashMap<>();
        linha.put("numero_linha", r.numero());
        for (String campo : TODAS) linha.put(campo, vazioParaNulo(r.valores().get(campo)));
        linha.put(FATURAMENTO, new BigDecimal(r.valores().get(FATURAMENTO)));
        linha.put("avisos", r.avisos());
        return linha;
    }

    private static String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto;
    }

    /** Só o nome do arquivo (sem pastas), no máximo 255 caracteres. */
    private static String nomeSeguro(String nome) {
        if (nome == null || nome.isBlank()) return "planilha";
        String base = nome.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").strip();
        if (base.isEmpty()) return "planilha";
        return base.length() > 255 ? base.substring(0, 255) : base;
    }
}
