package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProdutoAnalysisDTO {

    @Schema(example = "Carregador Wireless")
    @JsonProperty("nome_produto")
    private String nomeProduto;

    @Schema(example = "1006")
    @JsonProperty("quatidade_vendida")
    private Integer quantidadeVendida;

    @Schema(example = "288235871.00")
    @JsonProperty("total_arrecadado")
    private BigDecimal totalArrecadado;
}
