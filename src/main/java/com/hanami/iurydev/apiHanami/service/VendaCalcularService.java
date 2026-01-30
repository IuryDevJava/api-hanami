package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.dto.*;
import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.entity.embeddable.Cliente;
import com.hanami.iurydev.apiHanami.entity.enums.Genero;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
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

    public List<MetricasRegiaoDTO> calcularMetricasPorRegiao(List<Venda> vendas) {
        Map<String, List<Venda>> calculoPorRegiao = vendas.stream()
                .collect(Collectors.groupingBy(v -> v.getLogistica().getRegiao().name()));

        return calculoPorRegiao.entrySet().stream()
                .map(entry -> {
                    String nomeRegiao = entry.getKey();
                    List<Venda> listaRegiao = entry.getValue();

                    BigDecimal totalRegiao = BigDecimal.valueOf(calcularTotal(listaRegiao));
                    BigDecimal lucroRegiao = BigDecimal.valueOf(calcularLucro(listaRegiao));

                    return new MetricasRegiaoDTO(nomeRegiao, totalRegiao, lucroRegiao);

                })
                .toList();
    }

    public List<DistribuicaoDTO> calcularDistribuicaoPorGenero(List<Venda> vendas) {
        if (vendas.isEmpty())
            return Collections.emptyList();

        long totalVendas = vendas.size();

        Map<Genero, List<Venda>> agrupar = vendas.stream()
                .collect(Collectors.groupingBy(v -> v.getCliente().getGenero()));

        return agrupar.entrySet().stream()
                .map(entry -> {
                    String nomeGenero = (entry.getKey() != null) ? entry.getKey().name() : "Não informado";
                    long contagem = entry.getValue().size();

                    double percentual = Math.round((contagem * 100.0) / totalVendas * 100.0) / 100.0;

                    return new DistribuicaoDTO(nomeGenero, contagem, percentual);

                })
                .toList();
    }

    public List<DistribuicaoDTO> calcularDistribuicaoPorCidade(List<Venda> vendas) {
        if (vendas.isEmpty())
            return Collections.emptyList();

        long totalVendas = vendas.size();

        Map<String, List<Venda>> agrupar = vendas.stream()
                .collect(Collectors.groupingBy(v -> v.getCliente().getCidade()));

        return agrupar.entrySet().stream()
                .map(entry -> {
                    String nomeCidade = entry.getKey();
                    long contagem = entry.getValue().size();

                    double percentual = Math.round((contagem * 100.0) / totalVendas * 100.0) / 100.0;

                    return new DistribuicaoDTO(nomeCidade, contagem, percentual);

                })
                .toList();
    }

    public List<DistribuicaoDTO> calcularDistribuicaoPorFaixaEtaria(List<Venda> vendas) {
        if (vendas.isEmpty())
            return Collections.emptyList();

        long totalVendas = vendas.size();

        Map<String, List<Venda>> agrupar = vendas.stream()
                .collect(Collectors.groupingBy(v -> classificarFaixaEtaria(v.getCliente().getIdade())));

        return agrupar.entrySet().stream()
                .map(entry -> {
                    String faixaEtaria = entry.getKey();
                    long contagem = entry.getValue().size();

                    double percentual = Math.round((contagem * 100.0) / totalVendas * 100.0) / 100.0;

                    return new DistribuicaoDTO(faixaEtaria, contagem, percentual);

                })
                .toList();
    }


    public RelatorioDemograficoDTO relatorioDemografico(List<Venda> vendas) {
        RelatorioDemograficoDTO relatorio = new RelatorioDemograficoDTO();

        relatorio.setGenero(calcularDistribuicaoPorGenero(vendas));
        relatorio.setCidade(calcularDistribuicaoPorCidade(vendas));
        relatorio.setFaixaEtaria(calcularDistribuicaoPorFaixaEtaria(vendas));

        return relatorio;
    }

    private String classificarFaixaEtaria(Integer idade) {
        if (idade == null) return "Não informada";
        if (idade < 20) return "Menos de 20 anos";
        if (idade <= 30) return "20 a 30 anos";
        if (idade <= 45) return "31 a 45 anos";
        if (idade <= 60) return "46 a 60 anos";
        return "Mais de 60 anos";
    }
}
