package com.hanami.iurydev.apiHanami.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DistribuicaoDTO {

    private String categoria;
    private Long contagem;
    private Double percentual;
}
