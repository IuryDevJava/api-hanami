package com.hanami.iurydev.apiHanami.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MetricaFinanceiraDTO extends BaseFinanceiraDTO {

    @Schema(example = "5243176617.89")
    @JsonProperty("custo_total")
    private BigDecimal custoTotal;

    public MetricaFinanceiraDTO(BigDecimal receitaLiquida, BigDecimal lucroBruto, BigDecimal custoTotal) {
        super(receitaLiquida, lucroBruto);
        this.custoTotal = custoTotal;
    }


}
