package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class RelatorioDemograficoDTO {

    @JsonProperty("genero")
    private List<DistribuicaoDTO> genero;

    @JsonProperty("faixa_etaria")
    private List<DistribuicaoDTO> faixaEtaria;

    @JsonProperty("cidade")
    private List<DistribuicaoDTO> cidade;
}
