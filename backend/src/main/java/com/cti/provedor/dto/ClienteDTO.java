package com.cti.provedor.dto;

import java.util.Map;

/** Número da linha no Excel + texto de cada coluna. */
public record ClienteDTO(int numero, Map<String, String> campos) {}
