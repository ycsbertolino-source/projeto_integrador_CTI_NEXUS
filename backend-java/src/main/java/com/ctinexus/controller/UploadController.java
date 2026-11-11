package com.ctinexus.controller;

import com.ctinexus.dto.UploadRequest;
import com.ctinexus.dto.UploadResponse;
import com.ctinexus.service.UploadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/uploads")
    @SuppressWarnings("unchecked")
    public ResponseEntity<UploadResponse> receberUpload(@Valid @RequestBody UploadRequest request) {
        Map<String, Object> resultado = uploadService.processarUpload(request);
        Map<String, Object> estatisticas = resultado.get("estatisticas") instanceof Map<?, ?> map
                ? (Map<String, Object>) map : Map.of();
        Object rejeitadas = resultado.getOrDefault("rejeitadas", java.util.List.of());
        return ResponseEntity.ok(new UploadResponse(
                "Upload processado com sucesso.",
                String.valueOf(resultado.get("loteId")),
                request.getRecebidas(),
                ((Number) resultado.getOrDefault("validas", 0)).intValue(),
                ((Number) resultado.getOrDefault("invalidas", 0)).intValue(),
                estatisticas,
                rejeitadas
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
