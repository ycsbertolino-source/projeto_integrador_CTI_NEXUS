package com.ctinexus.dto;

import java.util.List;
import java.util.Map;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UploadRequest {
    private String arquivoNome;
    @Min(0)
    private int recebidas;
    @Min(0)
    private int invalidas;
    @Min(0)
    private int comAviso;
    @NotNull
    private List<Map<String, Object>> linhas;

    public String getArquivoNome() { return arquivoNome; }
    public void setArquivoNome(String arquivoNome) { this.arquivoNome = arquivoNome; }

    public int getRecebidas() { return recebidas; }
    public void setRecebidas(int recebidas) { this.recebidas = recebidas; }

    public int getInvalidas() { return invalidas; }
    public void setInvalidas(int invalidas) { this.invalidas = invalidas; }

    public int getComAviso() { return comAviso; }
    public void setComAviso(int comAviso) { this.comAviso = comAviso; }

    public List<Map<String, Object>> getLinhas() { return linhas; }
    public void setLinhas(List<Map<String, Object>> linhas) { this.linhas = linhas; }
}
