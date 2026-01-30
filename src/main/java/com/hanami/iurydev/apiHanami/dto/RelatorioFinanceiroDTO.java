package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RelatorioFinanceiroDTO extends BaseFinanceiraDTO {

    @Schema(example = "8342927976.00")
    @JsonProperty("total_vendas")
    private BigDecimal totalVendas;

    @Schema(example = "928849.70")
    @JsonProperty("media_por_transacao")
    private BigDecimal mediaPorTransacao;

    @Schema(example = "5243176617.89")
    @JsonProperty("custo_total")
    private BigDecimal custoTotal;

    @Schema(example = "8982")
    @JsonProperty("numero_transacoes")
    private Long numeroTransacoes;

    public RelatorioFinanceiroDTO(BigDecimal receitaLiquida, BigDecimal lucroBruto, BigDecimal totalVendas, BigDecimal mediaPorTransacao, BigDecimal custoTotal, Long numeroTransacoes) {
        super(receitaLiquida, lucroBruto);
        this.totalVendas = totalVendas;
        this.mediaPorTransacao = mediaPorTransacao;
        this.custoTotal = custoTotal; // Certifique-se que este campo existe na classe!
        this.numeroTransacoes = numeroTransacoes;
    }

}
