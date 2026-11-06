package com.cti.provedor.controller;

import com.cti.provedor.config.ApiException;
import com.cti.provedor.dto.ClienteDTO;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Qualquer erro volta no mesmo formato: {"erro": "mensagem", "campos": {...}}. */
@RestControllerAdvice
public class ErroHandler {

    private static final Logger log = LoggerFactory.getLogger(ErroHandler.class);

    public record ErroResposta(String erro, Map<String, String> campos) {}

    private static ResponseEntity<ErroResposta> resposta(HttpStatus status, String mensagem,
                                                         Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ErroResposta(mensagem, campos));
    }

    @ExceptionHandler(ApiException.class)  // os erros que nós mesmos lançamos
    public ResponseEntity<ErroResposta> api(ApiException e) {
        return resposta(e.getStatus(), e.getMessage(), null);
    }

    /**
     * Formato da aula: {"mensagem": "Dados inválidos", "campo": "faturamentoAnual", "erro": "O faturamento..."}
     * + "campos" com TODOS os erros (o front usa para mostrar tudo de uma vez).
     */
    private static ResponseEntity<Map<String, Object>> dadosInvalidos(Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("mensagem", "Dados inválidos");                       // mensagem geral
        var primeiro = campos.entrySet().stream().findFirst();          // o primeiro erro encontrado
        corpo.put("campo", primeiro.map(Map.Entry::getKey).orElse(null));      // campo que falhou
        corpo.put("erro", primeiro.map(Map.Entry::getValue).orElse("Dados inválidos")); // texto definido no DTO
        corpo.put("campos", campos);                                    // todos os erros
        return ResponseEntity.badRequest().body(corpo);                 // devolve HTTP 400
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)  // captura erro de validação do DTO (@Valid em um objeto)
    public ResponseEntity<Map<String, Object>> tratarErroDto(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> campos.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return dadosInvalidos(campos);
    }

    /**
     * Com List<@Valid ClienteDTO> (rota /api/clientes/validar) o Spring Boot 3 lança
     * HandlerMethodValidationException, e não MethodArgumentNotValidException.
     * O campo vem com a posição na lista: "[0].nomeCliente".
     */
    /** O validador não garante ordem; usamos a ordem dos campos do ClienteDTO (codigoCliente, nomeCliente...). */
    private static final List<String> CAMPOS_DTO = Arrays.stream(ClienteDTO.class.getRecordComponents())
            .map(RecordComponent::getName).toList();

    private static int ordemNoDto(String campo) {
        int i = CAMPOS_DTO.indexOf(campo);
        return i < 0 ? Integer.MAX_VALUE : i;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> tratarErroLista(HandlerMethodValidationException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (ParameterValidationResult r : e.getParameterValidationResults()) {
            String prefixo = r.getContainerIndex() != null ? "[" + r.getContainerIndex() + "]." : "";
            if (r instanceof ParameterErrors erros) {   // erros dentro de um ClienteDTO
                erros.getFieldErrors().stream()
                        .sorted(Comparator.comparingInt(fe -> ordemNoDto(fe.getField()))) // mesma ordem do DTO
                        .forEach(fe -> campos.putIfAbsent(prefixo + fe.getField(), fe.getDefaultMessage()));
            } else {                                     // erro no próprio parâmetro
                r.getResolvableErrors().forEach(m -> campos.putIfAbsent(
                        prefixo + r.getMethodParameter().getParameterName(), m.getDefaultMessage()));
            }
        }
        return dadosInvalidos(campos);
    }

    @ExceptionHandler(IllegalArgumentException.class) // captura erro lançado pelo Service (ex.: código repetido)
    public ResponseEntity<Map<String, String>> tratarRegraNegocio(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of( // devolve HTTP 400
                "mensagem", "Regra de validação não atendida", // mensagem geral
                "erro", e.getMessage() // mensagem específica, exemplo: código repetido
        )); // fim do body
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResposta> parametro(Exception e) {  // parâmetro ausente ou com valor errado
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro ausente ou inválido.", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)  // JSON ilegível ou valor do tipo errado
    public ResponseEntity<?> jsonIlegivel(HttpMessageNotReadableException e) {
        // Ex.: "dataContratacao": "30/02/2025" ou "faturamentoAnual": "abc" -> diz qual campo falhou
        if (e.getCause() instanceof MismatchedInputException m && !m.getPath().isEmpty()) {
            StringBuilder campo = new StringBuilder();
            for (JsonMappingException.Reference ref : m.getPath()) {
                if (ref.getFieldName() != null) campo.append(campo.isEmpty() ? "" : ".").append(ref.getFieldName());
                else if (ref.getIndex() >= 0) campo.append("[").append(ref.getIndex()).append("]");
            }
            String texto = campo.toString().endsWith("dataContratacao")
                    ? "A data de contratação deve ser uma data válida no formato aaaa-mm-dd"
                    : "Valor em formato inválido";
            Map<String, String> campos = new LinkedHashMap<>();
            campos.put(campo.toString(), texto);
            return dadosInvalidos(campos);
        }
        return resposta(HttpStatus.BAD_REQUEST, "O corpo da requisição não é um JSON válido.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)  // método errado
    public ResponseEntity<ErroResposta> metodo(HttpRequestMethodNotSupportedException e) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método " + e.getMethod() + " não é aceito nesta rota.", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)  // rota inexistente
    public ResponseEntity<ErroResposta> rotaInexistente(NoResourceFoundException e) {
        return resposta(HttpStatus.NOT_FOUND, "Rota não encontrada.", null);
    }

    @ExceptionHandler(Exception.class)  // qualquer outro: 500 sem expor detalhes
    public ResponseEntity<ErroResposta> inesperado(Exception e) {
        log.error("Erro inesperado", e);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente em instantes.", null);
    }
}
