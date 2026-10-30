package com.cti.provedor.config;

import org.springframework.http.HttpStatus;

/** O nosso erro com status HTTP: vira {"erro": "..."} no ErroHandler. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
