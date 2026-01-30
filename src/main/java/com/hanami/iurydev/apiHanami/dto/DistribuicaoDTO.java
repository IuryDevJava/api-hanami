package com.hanami.iurydev.apiHanami.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistribuicaoDTO {

    @Schema(example = "Brasília")
    private String categoria;

    @Schema(example = "421")
    private Long contagem;

    @Schema(example = "4.29")
    private Double percentual;
}
