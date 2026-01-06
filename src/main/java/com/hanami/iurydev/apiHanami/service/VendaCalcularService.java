package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.dto.MetricaFinanceiraDTO;
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
        List<Venda> validas = filtradosComSucessos(vendas);
        if (validas.isEmpty()) return criarRelatorioVazio();

        double totalVendas = calcularTotal(validas);
        double lucroBruto = calcularLucro(validas);
        double custoTotal = totalVendas - lucroBruto;
        long qtd = validas.size();

        return new RelatorioFinanceiroDTO(
                formatarMoeda(totalVendas - lucroBruto), // Receita Líquida
                formatarMoeda(lucroBruto),
                formatarMoeda(totalVendas),
                formatarMoeda(totalVendas / qtd),
                formatarMoeda(custoTotal),
                qtd
        );
    }

    public MetricaFinanceiraDTO calculaMetricas(List<Venda> vendas) {
        List<Venda> validas = filtradosComSucessos(vendas);
        if (validas.isEmpty()) return new MetricaFinanceiraDTO(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        double totalVendas = calcularTotal(validas);
        double lucroBruto = calcularLucro(validas);

        return new MetricaFinanceiraDTO(
                formatarMoeda(totalVendas - lucroBruto), // Receita Líquida
                formatarMoeda(lucroBruto),
                formatarMoeda(totalVendas - lucroBruto)  // Custo Total
        );
    }

    private List<Venda> filtradosComSucessos(List<Venda> vendas) {
        return vendas
                .stream()
                .filter(Venda::isProcessadoSucesso)
                .toList();
    }

    private double calcularTotal(List<Venda> validas) {
        return validas
                .stream()
                .mapToDouble(v -> v.getValorFinal().doubleValue())
                .sum();
    }

    private double calcularLucro(List<Venda> validas) {
        return validas
                .stream()
                .mapToDouble(v -> v.getValorFinal().doubleValue() * (v.getProduto().getMargemLucro() / 100))
                .sum();
    }

    private RelatorioFinanceiroDTO criarRelatorioVazio() {
        return new RelatorioFinanceiroDTO(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0L
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
