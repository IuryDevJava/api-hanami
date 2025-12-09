package com.hanami.iurydev.apiHanami.entity;

import com.hanami.iurydev.apiHanami.entity.embeddable.Cliente;
import com.hanami.iurydev.apiHanami.entity.embeddable.Logistica;
import com.hanami.iurydev.apiHanami.entity.embeddable.Produto;
import com.hanami.iurydev.apiHanami.entity.enums.CanalVenda;
import com.hanami.iurydev.apiHanami.entity.enums.FormaPagamento;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vendas")
@Data
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_transacao", nullable = false, unique = true)
    private String idTransacao;

    @Column(name = "data_venda", nullable = false)
    private LocalDate dataVenda;

    @Column(name = "valor_final")
    private BigDecimal valorFinal;

    private BigDecimal subtotal;

    @Column(name = "desconto_percent")
    private Double descontoPercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_venda")
    private CanalVenda canalVenda;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @Embedded
    private Cliente cliente;

    @Embedded
    private Produto produto;

    @Embedded
    private Logistica logistica;
}
