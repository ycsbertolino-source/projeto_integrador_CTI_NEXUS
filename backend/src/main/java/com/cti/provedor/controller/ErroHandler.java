package com.cti.provedor.controller;

import com.cti.provedor.config.ApiException;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)  // campos inválidos (@Valid)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> campos.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return resposta(HttpStatus.BAD_REQUEST, "Verifique os campos informados.", campos);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResposta> parametro(Exception e) {  // parâmetro ausente ou com valor errado
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro ausente ou inválido.", null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)  // JSON ilegível
    public ResponseEntity<ErroResposta> jsonIlegivel(HttpMessageNotReadableException e) {
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
