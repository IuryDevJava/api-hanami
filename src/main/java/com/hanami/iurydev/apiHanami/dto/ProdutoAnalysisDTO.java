package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProdutoAnalysisDTO {

    @JsonProperty("Nome_produto")
    private String nomeProduto;

    @JsonProperty("Quatidade_vendida")
    private Integer quantidadeVendida;

    @JsonProperty("Total_arrecadado")
    private BigDecimal totalArrecadado;
}
