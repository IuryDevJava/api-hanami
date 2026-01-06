package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseFinanceiraDTO {

    @JsonProperty("receita_liquida")
    private BigDecimal receitaLiquida;

    @JsonProperty("lucro_bruto")
    private BigDecimal lucroBruto;
}
