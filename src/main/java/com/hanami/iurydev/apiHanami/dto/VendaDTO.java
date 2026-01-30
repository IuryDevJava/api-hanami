package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VendaDTO {

    @Schema(example = "TXN00000001")
    @JsonProperty("id_transacao")
    @NotBlank
    @Pattern(regexp = "^TXN\\d{8}$", message = "ID Transação inválido. Esperado formato TXN...")
    private String idTransacao;

    @Schema(example = "2023-10-25")
    @JsonProperty("data_venda")
    @NotBlank(message = "Data da venda é obrigatória")
    private String dataVenda;

    @NotNull(message = "Valor final é obrigatório")
    @JsonProperty("valor_final")
    @PositiveOrZero
    private Double valorFinal;

    @PositiveOrZero
    private Double subtotal;

    @JsonProperty("desconto_percentual")
    @DecimalMin(value = "0.0", message = "Desconto não pode ser negativo")
    @DecimalMax(value = "30.0", message = "Desconto suspeito: máximo permitido é 30%")
    private Double descontoPercent;

    @JsonProperty("canal_venda")
    @NotBlank(message = "Canal de venda é obrigatório")
    private String canalVenda;

    @JsonProperty("forma_pagamento")
    @NotBlank(message = "Forma de pagamento é obrigatória")
    private String formaPagamento;

    // CLIENTES
    @Schema(example = "CLI000001")
    @JsonProperty("id_cliente")
    @NotBlank
    @Pattern(regexp = "^CLI\\d{6}$", message = "ID Cliente inválido. Esperado formato CLI...")
    private String clienteId;

    @NotBlank(message = "Nome do cliente é obrigatório")
    @JsonProperty("nome_cliente")
    private String nomeCliente;

    @JsonProperty("idade_cliente")
    @Min(value = 18, message = "Cliente deve ser maior de 18 anos")
    @Max(value = 100, message = "Idade inválida (verificar dados)")
    private Integer idadeCliente;

    @JsonProperty("genero_cliente")
    @Pattern(regexp = "^[MF]$", message = "Gênero deve ser 'M' ou 'F'")
    private String generoCliente;

    @JsonProperty("cidade_cliente")
    @NotBlank(message = "Cidade do cliente é obrigatória")
    private String cidadeCliente;

    @NotBlank
    @JsonProperty("estado_cliente")
    @Size(min = 2, max = 2, message = "Estado deve ser a sigla (ex: SP)")
    private String estadoCliente;

    @PositiveOrZero
    @JsonProperty("renda_estimada")
    private Double rendaEstimada;

    // PRODUTOS

    @Schema(example = "PRD001")
    @JsonProperty("id_produto")
    @NotBlank
    @Pattern(regexp = "^PRD\\d{3}$", message = "ID Produto inválido. Esperado formato PRD...")
    private String produtoId;

    @NotBlank(message = "Nome do produto é obrigatório")
    @JsonProperty("nome_produto")
    private String nomeProduto;

    private String categoria;

    private String marca;

    @JsonProperty("preco_unitario")
    @PositiveOrZero(message = "Preço unitário não pode ser negativo")
    private Double precoUnitario;

    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantidade;

    @JsonProperty("margem_lucro")
    @DecimalMin(value = "15.0", message = "Margem de lucro abaixo do permitido (15%)")
    @DecimalMax(value = "60.0", message = "Margem de lucro acima do permitido (60%)")
    private Double margemLucro;

    // LOGÍSTICA E OPERAÇÕES

    @NotBlank(message = "Região é obrigatória")
    private String regiao;

    @JsonProperty("status_entrega")
    private String statusEntrega;

    @JsonProperty("tempo_entrega_dias")
    @Min(value = 1, message = "Tempo de entrega mínimo é 1 dia")
    @Max(value = 30, message = "Tempo de entrega excede o limite operacional")
    private Integer tempoEntregaDias;

    @Schema(example = "VEN001")
    @JsonProperty("id_vendedor")
    @NotBlank
    @Pattern(regexp = "^VEN\\d{3}$", message = "ID Vendedor inválido. Esperado formato VEN...")
    private String vendedorId;
}
