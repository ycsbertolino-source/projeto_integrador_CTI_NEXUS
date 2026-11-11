package com.ctinexus.controller;

import com.ctinexus.service.DashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestParam(required = false) String loteId
    ) {
        Map<String, Object> resultado = dashboardService.obterDashboard(loteId);
        if (resultado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "Nenhum lote processado encontrado."
            ));
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/lotes")
    public List<Map<String, Object>> lotes() {
        return dashboardService.listarLotes();
    }
}
