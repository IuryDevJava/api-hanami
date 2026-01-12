package com.hanami.iurydev.apiHanami.dto;

import lombok.Data;

import java.util.List;

@Data
public class RelatorioDemograficoDTO {

    private List<DistribuicaoDTO> porGenero;
    private List<DistribuicaoDTO> porFaixaEtaria;
    private List<DistribuicaoDTO> porCidade;
}
