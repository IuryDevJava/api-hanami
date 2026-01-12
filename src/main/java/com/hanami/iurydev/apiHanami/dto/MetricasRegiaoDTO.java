package com.hanami.iurydev.apiHanami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MetricasRegiaoDTO {

    private String regiao;
    private BigDecimal valorTotal;
    private BigDecimal lucroTotal;

}
