package com.hanami.iurydev.apiHanami.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VendaDTO {

    @NotBlank
    @Pattern(regexp = "^TXN\\d{8}$", message = "ID Transação inválido. Esperado formato TXN...")
    private String idTransacao;

    @NotBlank(message = "Data da venda é obrigatória")
    private String dataVenda;

    @NotNull(message = "Valor final é obrigatório")
    @PositiveOrZero
    private Double valorFinal;

    @PositiveOrZero
    private Double subtotal;

    @DecimalMin(value = "0.0", message = "Desconto não pode ser negativo")
    @DecimalMax(value = "30.0", message = "Desconto suspeito: máximo permitido é 30%")
    private Double descontoPercent;

    @NotBlank(message = "Canal de venda é obrigatório")
    private String canalVenda;

    @NotBlank(message = "Forma de pagamento é obrigatória")
    private String formaPagamento;

    // CLIENTES

    @NotBlank
    @Pattern(regexp = "^CLI\\d{6}$", message = "ID Cliente inválido. Esperado formato CLI...")
    private String clienteId;

    @NotBlank(message = "Nome do cliente é obrigatório")
    private String nomeCliente;

    @Min(value = 18, message = "Cliente deve ser maior de 18 anos")
    @Max(value = 100, message = "Idade inválida (verificar dados)")
    private Integer idadeCliente;

    @Pattern(regexp = "^[MF]$", message = "Gênero deve ser 'M' ou 'F'")
    private String generoCliente;

    @NotBlank(message = "Cidade do cliente é obrigatória")
    private String cidadeCliente;

    @NotBlank
    @Size(min = 2, max = 2, message = "Estado deve ser a sigla (ex: SP)")
    private String estadoCliente;

    @PositiveOrZero
    private Double rendaEstimada;

    // PRODUTOS

    @NotBlank
    @Pattern(regexp = "^PRD\\d{3}$", message = "ID Produto inválido. Esperado formato PRD...")
    private String produtoId;

    @NotBlank(message = "Nome do produto é obrigatório")
    private String nomeProduto;

    private String categoria;

    private String marca;

    @PositiveOrZero(message = "Preço unitário não pode ser negativo")
    private Double precoUnitario;

    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private Integer quantidade;

    @DecimalMin(value = "15.0", message = "Margem de lucro abaixo do permitido (15%)")
    @DecimalMax(value = "60.0", message = "Margem de lucro acima do permitido (60%)")
    private Double margemLucro;

    // LOGÍSTICA E OPERAÇÕES

    @NotBlank(message = "Região é obrigatória")
    private String regiao;

    private String statusEntrega;

    @Min(value = 1, message = "Tempo de entrega mínimo é 1 dia")
    @Max(value = 30, message = "Tempo de entrega excede o limite operacional")
    private Integer tempoEntregaDias;

    @NotBlank
    @Pattern(regexp = "^VEN\\d{3}$", message = "ID Vendedor inválido. Esperado formato VEN...")
    private String vendedorId;
}
