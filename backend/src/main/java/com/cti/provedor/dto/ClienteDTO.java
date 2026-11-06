package com.cti.provedor.dto; // pacote onde o DTO ficará organizado

import java.time.LocalDate; // usado para representar data sem hora

import jakarta.validation.constraints.NotBlank; // valida texto obrigatório
import jakarta.validation.constraints.NotNull; // valida valor obrigatório
import jakarta.validation.constraints.Pattern; // valida formato do texto
import jakarta.validation.constraints.Positive; // valida número maior que zero

// DTO significa Data Transfer Object.
// Ele representa os dados que chegam do front-end para o backend.
// O DTO não salva no banco. Ele apenas recebe os dados enviados pelo Pinia e aplica regras de validação.
public record ClienteDTO( // record cria uma classe simples para transportar dados

        @NotBlank(message = "O código do cliente é obrigatório") // não aceita vazio
        String codigoCliente, // vem da coluna codigo_cliente da planilha

        @NotBlank(message = "O nome do cliente é obrigatório") // obriga nome preenchido
        String nomeCliente, // vem da coluna nome_cliente

        @NotBlank(message = "O consultor é obrigatório") // obriga consultor preenchido
        String consultor, // vem da coluna consultor

        @NotBlank(message = "O segmento é obrigatório") // obriga segmento preenchido
        String segmento, // vem da coluna segmento

        @NotBlank(message = "O nível do cliente é obrigatório") // não aceita vazio
        @Pattern(regexp = "[ABC]", message = "O nível deve ser A, B ou C") // só aceita A, B ou C
        String nivelCliente, // vem da coluna nivel_cliente

        @NotNull(message = "O faturamento anual é obrigatório") // não aceita nulo
        @Positive(message = "O faturamento deve ser maior que zero") // rejeita zero e negativo
        Double faturamentoAnual, // vem da coluna faturamento_anual

        @NotBlank(message = "Informe os serviços contratados") // precisa ter pelo menos um serviço
        String servicosContratados, // vem da coluna servicos_contratados

        @NotNull(message = "A data de contratação é obrigatória") // data não pode ser nula
        LocalDate dataContratacao, // vem da coluna data_contratacao (formato aaaa-mm-dd)

        String cidade, // campo opcional: pode vir vazio

        @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "A UF deve possuir duas letras") // vazio ou 2 letras
        String uf // campo opcional, mas se vier preenchido precisa ser SP, RJ, MG...
) { // fecha o record
} // fecha o DTO
