package com.cti.provedor.controller;

import com.cti.provedor.dto.UploadDtos.EnvioValidadasRequest;
import com.cti.provedor.service.UploadService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Recebe o POST com as linhas do front e entrega tudo ao UploadService. */
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService service;

    public UploadController(UploadService service) {
        this.service = service;  // o Spring entrega o serviço
    }

    @PostMapping("/validadas")            // POST /api/uploads/validadas
    @ResponseStatus(HttpStatus.CREATED)   // responde 201
    public Map<String, Object> enviarValidadas(@RequestBody EnvioValidadasRequest pedido) {
        return service.receberValidadas(pedido);
    }
}
