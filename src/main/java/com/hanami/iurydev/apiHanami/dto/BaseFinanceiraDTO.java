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

    @JsonProperty("Receita_liquida")
    private BigDecimal receitaLiquida;

    @JsonProperty("Lucro_bruto")
    private BigDecimal lucroBruto;
}
