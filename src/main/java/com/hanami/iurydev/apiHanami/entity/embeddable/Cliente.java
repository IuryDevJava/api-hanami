package com.hanami.iurydev.apiHanami.entity.embeddable;

import com.hanami.iurydev.apiHanami.entity.enums.Genero;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.math.BigDecimal;


@Embeddable
@Data
public class Cliente {

    @Column(name = "cliente_id")
    private String clienteId;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(name = "idade_cliente")
    private Integer idade;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero_cliente", length = 1)
    private Genero genero;

    @Column(name = "cidade_cliente")
    private String cidade;

    @Column(name = "estado_cliente", length = 2)
    private String estado;

    @Column(name = "renda_estimada")
    private BigDecimal rendaEstimada;
}
