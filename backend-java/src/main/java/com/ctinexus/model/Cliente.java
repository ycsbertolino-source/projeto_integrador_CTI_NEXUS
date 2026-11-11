package com.ctinexus.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigoCliente;

    @Column(nullable = false)
    private String nomeCliente;

    @Column(nullable = false)
    private String consultor;

    @Column(nullable = false)
    private String segmento;

    @Column(nullable = false)
    private String nivelCliente;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal faturamentoAnual;

    @Column(nullable = false)
    private String servicosContratados;

    @Column(nullable = false)
    private LocalDate dataContratacao;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    public Cliente() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getConsultor() { return consultor; }
    public void setConsultor(String consultor) { this.consultor = consultor; }

    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }

    public String getNivelCliente() { return nivelCliente; }
    public void setNivelCliente(String nivelCliente) { this.nivelCliente = nivelCliente; }

    public BigDecimal getFaturamentoAnual() { return faturamentoAnual; }
    public void setFaturamentoAnual(BigDecimal faturamentoAnual) { this.faturamentoAnual = faturamentoAnual; }

    public String getServicosContratados() { return servicosContratados; }
    public void setServicosContratados(String servicosContratados) { this.servicosContratados = servicosContratados; }

    public LocalDate getDataContratacao() { return dataContratacao; }
    public void setDataContratacao(LocalDate dataContratacao) { this.dataContratacao = dataContratacao; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
}
