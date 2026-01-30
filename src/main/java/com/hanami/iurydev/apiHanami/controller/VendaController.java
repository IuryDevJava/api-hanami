package com.hanami.iurydev.apiHanami.controller;

import com.hanami.iurydev.apiHanami.dto.*;
import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.repository.VendaRepository;
import com.hanami.iurydev.apiHanami.service.ReadFileService;
import com.hanami.iurydev.apiHanami.service.VendaCalcularService;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Projeto Hanami", description = "API de Análise de Dados")
@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
@Slf4j
public class VendaController {

    private final ReadFileService readFileService;
    private final VendaCalcularService vendaCalcularService;
    private final VendaRepository vendaRepository;

    @Operation(
            summary = "Upload",
            description = "Faz o upload de arquivo CSV/XLSX"
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadDTO> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) {

        if (file == null || file.isEmpty()) {
            log.error("Erro 400. Ao tentar fazer o upload sem arquivo foi retornado um erro");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UploadDTO("erro", 0));
        }

        try {
            List<Venda> processados = readFileService.readFile(file);
            log.info("200 OK. Arquivo '{}' foi processado com sucesso. Total: {} linhas", file.getOriginalFilename(), processados.size());

            if (processados.isEmpty()) {
                return ResponseEntity.ok(new UploadDTO("Aviso: Nenhuma nova linha processada", 0));
            }

            // Retorna 200
            return ResponseEntity.ok(new UploadDTO("sucesso", processados.size()));

        } catch (IllegalArgumentException e) {
            log.error("Erro 422. Arquivo enviado não contém uma ou mais colunas obrigatórias {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new UploadDTO(e.getMessage(), 0));
        } catch (Exception e) {
            log.error("Erro crítico durante o processamento de upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadDTO("Erro interno ao processar o arquivo", 0));
        }
    }

    @Operation(
            summary = "Relatório de Vendas",
            description = "Retorna um JSON com o receita_liquida, lucro_bruto, total_vendas, media_por_transacao, custo_total, numero_transacoes"
    )
    @GetMapping("/reports/sales-summary")
    public ResponseEntity<RelatorioFinanceiroDTO> getSalesSumary() {
        List<Venda> vendas = vendaRepository.findAll();
        return ResponseEntity.ok(vendaCalcularService.calculaFinanceiro(vendas));
    }

    @Operation(
            summary = "Relatório de Produtos",
            description = "Retorna um JSON com uma lista de produtos"
    )
    @GetMapping("/reports/product-analysis")
    public ResponseEntity<List<ProdutoAnalysisDTO>> getProductAnalisys(
            @Parameter(description = "Critério de ordenação", example = "nome", schema = @Schema(allowableValues = {"nome", "preco", "quantidade"}))
            @RequestParam(value = "sort_by", required = false) String sortBy) {
        List<Venda> vendas = vendaRepository.findAll();
        List<ProdutoAnalysisDTO> analise = vendaCalcularService.analisarProdutos(vendas, sortBy);
        return ResponseEntity.ok(analise);
    }

    @Operation(
            summary = "Métricas Financeiras",
            description = "Retorna um JSON com lucro_bruto, receita_liquida e custo_total calculados"
    )
    @GetMapping("/reports/financial-metrics")
    public ResponseEntity<MetricaFinanceiraDTO> getFinancialMetrics() {
        List<Venda> vendas = vendaRepository.findAll();
        MetricaFinanceiraDTO metricas = vendaCalcularService.calculaMetricas(vendas);
        return ResponseEntity.ok(metricas);
    }

    @Operation(
            summary = "Clientes e Região",
            description = "Retorna um JSON com cada região como chave e suas métricas"
    )
    @GetMapping("/reports/regional-performance")
    public ResponseEntity<Map<String, MetricasRegiaoDTO>> getRegionalPerformance() {
        List<Venda> vendas = vendaRepository.findByProcessadoSucessoTrue();
        List<MetricasRegiaoDTO> lista = vendaCalcularService.calcularMetricasPorRegiao(vendas);

        Map<String, MetricasRegiaoDTO> mapa = lista.stream()
                .collect(Collectors.toMap(
                        MetricasRegiaoDTO::getRegiao,
                        dto -> dto
                ));

        return ResponseEntity.ok(mapa);
    }

    @Operation(
            summary = "Distribuição Demográfica",
            description = "Retorna um JSON com as distribuições demográficas"
    )
    @GetMapping("/reports/customer-profile")
    public ResponseEntity<RelatorioDemograficoDTO> getCustomerProfile() {
        List<Venda> vendasSucesso = vendaRepository.findByProcessadoSucessoTrue();
        RelatorioDemograficoDTO relatorio = vendaCalcularService.relatorioDemografico(vendasSucesso);

        return ResponseEntity.ok(relatorio);
    }

    @Operation(
            summary = "Relatórios Exportáveis (JSON/PDF)",
            description = "Retorna um arquivo report.json ou report.pdf para download"
    )
    @GetMapping("/reports/download")
    public ResponseEntity<byte[]> downloadRelatorio(
            @Parameter(description = "Formato do arquivo", required = true, schema = @Schema(allowableValues = {"json", "pdf"}))
            @RequestParam String format) {
        if (!format.equalsIgnoreCase("pdf") && !format.equalsIgnoreCase("json")) {
            return ResponseEntity.badRequest().build();
        }

        List<Venda> vendas = vendaRepository.findByProcessadoSucessoTrue();

        if (format.equalsIgnoreCase("json")) {
            return downloadJson(vendas);
        }
        return downloadPdf(vendas);

    }

    private ResponseEntity<byte[]> downloadJson(List<Venda> vendas) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(vendas);
            byte[] dados = json.getBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.json\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<byte[]> downloadPdf(List<Venda> vendas) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4);
            PdfWriter.getInstance(documento, outputStream);
            documento.open();

            Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL);
            documento.add(new Paragraph("Relatório", fonteTitulo));
            documento.add(new Paragraph(" "));

            PdfPTable tabela = new PdfPTable(3);
            tabela.addCell("ID Transação");
            tabela.addCell("Cliente");
            tabela.addCell("Valor Total");

            for (Venda v : vendas) {
                tabela.addCell(v.getIdTransacao());
                tabela.addCell(v.getCliente().getNomeCliente());
                tabela.addCell("R$ " + v.getValorFinal());
            }

            documento.add(tabela);

            documento.add(new Paragraph(" ")); // Espaço em branco
            documento.add(new Paragraph("Desempenho por Região", fonteTitulo));

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            Map<String, Double> vendasPorRegiao = vendas.stream()
                    .collect(Collectors.groupingBy(
                            venda -> venda.getCliente().getEstado(),
                            Collectors.summingDouble(venda -> venda.getValorFinal().doubleValue())
                    ));

            vendasPorRegiao.forEach((regiao, total) -> {
                dataset.addValue(total, "Vendas", regiao);
            });

            JFreeChart grafico = ChartFactory.createBarChart(
                    "Vendas por Região", "Região", "Total (R$)",
                    dataset, PlotOrientation.VERTICAL, false, true, false
            );

            int largura = 500;
            int altura = 300;
            BufferedImage bufferedImage = grafico.createBufferedImage(largura, altura);
            com.lowagie.text.Image imagemGrafico = com.lowagie.text.Image.getInstance(bufferedImage, null);
            imagemGrafico.setAlignment(Element.ALIGN_CENTER);

            documento.add(imagemGrafico);
            documento.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}