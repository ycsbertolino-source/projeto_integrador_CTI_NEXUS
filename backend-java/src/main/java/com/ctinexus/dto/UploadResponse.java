package com.ctinexus.dto;

import java.util.Map;

public record UploadResponse(
        String message,
        String loteId,
        int totalLinhas,
        int validas,
        int invalidas,
        Map<String, Object> estatisticas,
        Object rejeitadas
) {
}
