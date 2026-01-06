package com.hanami.iurydev.apiHanami.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MetricaFinanceiraDTO extends BaseFinanceiraDTO {

    @JsonProperty("custo_total")
    private BigDecimal custoTotal;

    public MetricaFinanceiraDTO(BigDecimal receitaLiquida, BigDecimal lucroBruto, BigDecimal custoTotal) {
        super(receitaLiquida, lucroBruto);
        this.custoTotal = custoTotal;
    }


}
