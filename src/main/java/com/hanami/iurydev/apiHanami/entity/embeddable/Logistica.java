package com.hanami.iurydev.apiHanami.entity.embeddable;

import com.hanami.iurydev.apiHanami.entity.enums.Regiao;
import com.hanami.iurydev.apiHanami.entity.enums.StatusEntrega;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Embeddable
@Data
public class Logistica {

    @Enumerated(EnumType.STRING)
    private Regiao regiao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_entrega")
    private StatusEntrega statusEntrega;

    @Column(name = "tempo_entrega_dias")
    private Integer tempoEntregaDias;

    @Column(name = "vendedor_id")
    private String vendedorId;
}
