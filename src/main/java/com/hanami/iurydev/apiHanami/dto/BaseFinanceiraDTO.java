package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseFinanceiraDTO {

    @Schema(example = "5243176617.89")
    @JsonProperty("receita_liquida")
    private BigDecimal receitaLiquida;

    @Schema(example = "3099751358.11")
    @JsonProperty("lucro_bruto")
    private BigDecimal lucroBruto;
}
