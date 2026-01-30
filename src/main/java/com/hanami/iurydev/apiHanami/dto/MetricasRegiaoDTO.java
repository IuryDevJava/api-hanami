package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MetricasRegiaoDTO {

    @Schema(example = "NORTE")
    private String regiao;

    @Schema(example = "1682054698")
    @JsonProperty("valor_total")
    private BigDecimal valorTotal;

    @Schema(example = "624613955.94")
    @JsonProperty("lucro_total")
    private BigDecimal lucroTotal;

}
