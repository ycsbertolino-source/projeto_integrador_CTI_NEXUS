package com.cti.provedor.controller;

import com.cti.provedor.service.DashboardService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> obterDashboard(
            @RequestParam(required = false) Long loteId
    ) {
        Map<String, Object> resultado = dashboard.obterDashboard(loteId);
        if (resultado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nenhum lote processado encontrado."));
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/lotes")
    public List<Map<String, Object>> listarLotes() {
        return dashboard.listarLotes();
    }
}
