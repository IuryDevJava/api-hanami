package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RelatorioDemograficoDTO {

    @JsonProperty("Genero")
    private List<DistribuicaoDTO> genero;

    @JsonProperty("Faixa_etaria")
    private List<DistribuicaoDTO> faixaEtaria;

    @JsonProperty("Cidade")
    private List<DistribuicaoDTO> cidade;
}
