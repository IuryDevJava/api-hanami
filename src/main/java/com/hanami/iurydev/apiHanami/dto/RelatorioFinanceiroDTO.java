package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RelatorioFinanceiroDTO {

    @JsonProperty("receita_liquida")
    private BigDecimal receitaLiquida;

    @JsonProperty("lucro_bruto")
    private BigDecimal lucroBruto;

    @JsonProperty("total_vendas")
    private BigDecimal totalVendas;

    @JsonProperty("media_por_transacao")
    private BigDecimal mediaPorTransacao;

    @JsonProperty("numero_transacoes")
    private Long numeroTransacoes;
}
