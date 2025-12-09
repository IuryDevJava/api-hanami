package com.hanami.iurydev.apiHanami.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.math.BigDecimal;

@Embeddable
@Data
public class Produto {

    @Column(name = "produto_id")
    private String produtoId;

    @Column(name = "nome_produto")
    private String nomeProduto;

    private String categoria;
    private String marca;

    @Column(name = "preco_unitario")
    private BigDecimal precoUnitario;

    private Integer quantidade;

    @Column(name = "margem_lucro")
    private Double margemLucro;
}
