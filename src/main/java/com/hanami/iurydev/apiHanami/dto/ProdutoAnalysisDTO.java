package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProdutoAnalysisDTO {

    @JsonProperty("nome_produto")
    private String nomeProduto;

    @JsonProperty("quatidade_vendida")
    private Integer quantidadeVendida;

    @JsonProperty("total_arrecadado")
    private BigDecimal totalArrecadado;
}
