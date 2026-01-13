package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RelatorioFinanceiroDTO extends BaseFinanceiraDTO {

    @JsonProperty("Total_vendas")
    private BigDecimal totalVendas;

    @JsonProperty("Media_por_transacao")
    private BigDecimal mediaPorTransacao;

    @JsonProperty("Custo_total")
    private BigDecimal custoTotal;

    @JsonProperty("Numero_transacoes")
    private Long numeroTransacoes;

    public RelatorioFinanceiroDTO(BigDecimal receitaLiquida, BigDecimal lucroBruto, BigDecimal totalVendas, BigDecimal mediaPorTransacao, BigDecimal custoTotal, Long numeroTransacoes) {
        super(receitaLiquida, lucroBruto);
        this.totalVendas = totalVendas;
        this.mediaPorTransacao = mediaPorTransacao;
        this.custoTotal = custoTotal; // Certifique-se que este campo existe na classe!
        this.numeroTransacoes = numeroTransacoes;
    }

}
