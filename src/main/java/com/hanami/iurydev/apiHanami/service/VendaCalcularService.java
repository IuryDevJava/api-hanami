package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.dto.ProdutoAnalysisDTO;
import com.hanami.iurydev.apiHanami.dto.RelatorioFinanceiroDTO;
import com.hanami.iurydev.apiHanami.entity.Venda;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VendaCalcularService {

    public RelatorioFinanceiroDTO calculaFinanceiro(List<Venda> vendas) {
        if (vendas.isEmpty()) {
            return new RelatorioFinanceiroDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L);
        }

        List<Venda> validas = vendas.stream().filter(Venda::isProcessadoSucesso).toList();

        double totalVendas = validas.stream()
                .mapToDouble(v -> v.getValorFinal().doubleValue())
                .sum();

        long numeroTransacoes = (long) validas.size();

        double media = numeroTransacoes > 0 ? totalVendas / numeroTransacoes : 0.0;

        double lucroBruto = validas.stream()
                .mapToDouble(v -> v.getValorFinal().doubleValue() * (v.getProduto().getMargemLucro() / 100))
                .sum();

        double receitaLiquida = totalVendas - lucroBruto;

        // Convertendo para BigDecimal com arredondamento de 2 casas
        return new RelatorioFinanceiroDTO(
                formatarMoeda(receitaLiquida),
                formatarMoeda(lucroBruto),
                formatarMoeda(totalVendas),
                formatarMoeda(media),
                numeroTransacoes
        );
    }

    private BigDecimal formatarMoeda(Double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public List<ProdutoAnalysisDTO> analisarProdutos(List<Venda> vendas, String sortBy) {
        Map<String, List<Venda>> vendasPorProduto = vendas.stream()
                .filter(Venda::isProcessadoSucesso)
                .collect(Collectors.groupingBy(v -> v.getProduto().getNomeProduto()));

        List<ProdutoAnalysisDTO> resultado = vendasPorProduto.entrySet().stream()
                .map(entry -> {
                    String nome = entry.getKey();
                    List<Venda> vendasDoProduto = entry.getValue();

                    int totalQtd = vendasDoProduto.stream()
                            .mapToInt(v -> v.getProduto().getQuantidade())
                            .sum();

                    BigDecimal totalValor = vendasDoProduto.stream()
                            .map(Venda::getValorFinal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(2, RoundingMode.HALF_UP);

                    return new ProdutoAnalysisDTO(nome, totalQtd, totalValor);
                })
                .collect(Collectors.toList());

        if ("quantidade".equalsIgnoreCase(sortBy)) {
            resultado.sort(Comparator.comparing(ProdutoAnalysisDTO::getQuantidadeVendida).reversed());
        } else if ("valor".equalsIgnoreCase(sortBy)) {
            resultado.sort(Comparator.comparing(ProdutoAnalysisDTO::getTotalArrecadado).reversed());
        }

        return resultado;
    }
}
