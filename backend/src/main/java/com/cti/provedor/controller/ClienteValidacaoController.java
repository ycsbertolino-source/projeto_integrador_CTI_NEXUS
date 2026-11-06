package com.cti.provedor.controller; // pacote dos controllers

import java.util.List; // permite receber lista de clientes

import org.springframework.http.ResponseEntity; // representa resposta HTTP
import org.springframework.web.bind.annotation.PostMapping; // cria rota POST
import org.springframework.web.bind.annotation.RequestBody; // lê o corpo JSON
import org.springframework.web.bind.annotation.RequestMapping; // define caminho base
import org.springframework.web.bind.annotation.RestController; // classe responde como API REST

import com.cti.provedor.dto.ClienteDTO; // DTO com as validações
import com.cti.provedor.service.ClienteValidacaoService; // service com regra de lote
import jakarta.validation.Valid; // executa as validações do DTO

// Rota que o Pinia/Axios chama: recebe o JSON, valida (DTO + Service) e devolve a lista preparada.
// Sem Repository, sem Entity, sem banco e sem Python nesta etapa.
@RestController // informa que esta classe devolve dados em JSON
@RequestMapping("/api/clientes") // caminho inicial das rotas deste controller
public class ClienteValidacaoController { // classe responsável pela validação de clientes

    private final ClienteValidacaoService service; // service que valida o lote

    public ClienteValidacaoController(ClienteValidacaoService service) { // injeção de dependência
        this.service = service; // guarda o service recebido pelo Spring
    }

    @PostMapping("/validar") // rota POST /api/clientes/validar
    public ResponseEntity<List<ClienteDTO>> validarClientes( // devolve lista validada
            @RequestBody List<@Valid ClienteDTO> clientes) { // valida cada item da lista

        List<ClienteDTO> preparados = service.validarEPreparar(clientes); // valida lote e padroniza
        return ResponseEntity.ok(preparados); // devolve HTTP 200 com os dados preparados
    }
} // fim do controller
