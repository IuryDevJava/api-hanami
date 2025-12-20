package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.dto.VendaDTO;
import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.entity.embeddable.Cliente;
import com.hanami.iurydev.apiHanami.entity.embeddable.Logistica;
import com.hanami.iurydev.apiHanami.entity.embeddable.Produto;
import com.hanami.iurydev.apiHanami.entity.enums.*;
import com.hanami.iurydev.apiHanami.repository.VendaRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReadFileService {

    private final VendaRepository vendaRepository;
    private final Validator validator;

    @Transactional
    public List<Venda> readFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        List<VendaDTO> dtos;

        if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
            dtos = readCSV(file);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
            dtos = readExcel(file);
        } else {
            throw new IllegalArgumentException("Formato inválido. Envie .csv ou .xlsx");
        }

        return processarEConverter(dtos);
    }

    // --- LEITURA CSV ---
    private List<VendaDTO> readCSV(MultipartFile file) throws IOException {
        List<VendaDTO> listaDtos = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            // Lê o cabeçalho para descobrir onde está cada coluna
            String[] headers = csvReader.readNext();
            if (headers == null) throw new IOException("Arquivo CSV vazio");

            Map<String, Integer> mapaColunas = mapearCabecalho(headers);

            String[] linha;
            while ((linha = csvReader.readNext()) != null) {
                String[] linhaFinal = linha;
                VendaDTO dto = criarDtoApartirDaLinha(mapaColunas, (index) ->
                        (index < linhaFinal.length) ? linhaFinal[index] : null
                );
                listaDtos.add(dto);
            }
        } catch (CsvValidationException e) {
            throw new IOException("Erro de validação na leitura do CSV", e);
        }
        return listaDtos;
    }

    // LEITURA EXCEL
    private List<VendaDTO> readExcel(MultipartFile file) throws IOException {
        List<VendaDTO> listaDtos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) return listaDtos;

            // Lê Cabeçalho
            Row headerRow = rowIterator.next();
            Map<String, Integer> mapaColunas = mapearCabecalhoExcel(headerRow);
            DataFormatter formatter = new DataFormatter();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                VendaDTO dto = criarDtoApartirDaLinha(mapaColunas, (index) ->
                        formatter.formatCellValue(row.getCell(index))
                );
                listaDtos.add(dto);
            }
        }
        return listaDtos;
    }

    @FunctionalInterface
    interface LeitorValor {
        String get(Integer index);
    }

    private VendaDTO criarDtoApartirDaLinha(Map<String, Integer> mapa, LeitorValor leitor) {
        VendaDTO dto = new VendaDTO();

        var util = new Object() {
            String txt(String nomeCol) {
                Integer idx = mapa.get(nomeCol);
                return (idx != null) ? leitor.get(idx) : null;
            }
            Double num(String nomeCol) {
                String v = txt(nomeCol);
                if (v == null || v.isBlank()) return null;
                try {
                    return Double.parseDouble(v.replace("R$", "").replace(",", ".").trim());
                } catch (Exception e) { return null; }
            }
            Integer inteiro(String nomeCol) {
                Double v = num(nomeCol);
                return (v != null) ? v.intValue() : null;
            }
        };

        // Mapeamento exato dos campos do DTO
        dto.setIdTransacao(util.txt("id_transacao"));
        dto.setDataVenda(util.txt("data_venda"));
        dto.setValorFinal(util.num("valor_final"));
        dto.setSubtotal(util.num("subtotal"));
        dto.setDescontoPercent(util.num("desconto_percent"));
        dto.setCanalVenda(util.txt("canal_venda"));
        dto.setFormaPagamento(util.txt("forma_pagamento"));

        // Cliente
        dto.setClienteId(util.txt("cliente_id"));
        dto.setNomeCliente(util.txt("nome_cliente"));
        dto.setIdadeCliente(util.inteiro("idade_cliente"));
        dto.setGeneroCliente(util.txt("genero_cliente"));
        dto.setCidadeCliente(util.txt("cidade_cliente"));
        dto.setEstadoCliente(util.txt("estado_cliente"));
        dto.setRendaEstimada(util.num("renda_estimada"));

        // Produto
        dto.setProdutoId(util.txt("produto_id"));
        dto.setNomeProduto(util.txt("nome_produto"));
        dto.setCategoria(util.txt("categoria"));
        dto.setMarca(util.txt("marca"));
        dto.setPrecoUnitario(util.num("preco_unitario"));
        dto.setQuantidade(util.inteiro("quantidade"));
        dto.setMargemLucro(util.num("margem_lucro"));

        // Logistica
        dto.setRegiao(util.txt("regiao"));
        dto.setStatusEntrega(util.txt("status_entrega"));
        dto.setTempoEntregaDias(util.inteiro("tempo_entrega_dias"));
        dto.setVendedorId(util.txt("vendedor_id"));

        return dto;
    }

    // --- CONVERSÃO E SALVAMENTO ---
    private List<Venda> processarEConverter(List<VendaDTO> dtos) {
        List<Venda> vendasParaSalvar = new ArrayList<>();

        for (VendaDTO dto : dtos) {
            // 1. Validação (Pula se o DTO estiver inválido)
            Set<ConstraintViolation<VendaDTO>> erros = validator.validate(dto);
            if (!erros.isEmpty()) {
                // Aqui você poderia guardar os erros para retornar ao controller
                continue;
            }

            // 2. Converter DTO -> Entidade
            try {
                Venda venda = convertDtoToEntity(dto);
                vendasParaSalvar.add(venda);
            } catch (Exception e) {
            }
        }

        return vendaRepository.saveAll(vendasParaSalvar);
    }

    private Venda convertDtoToEntity(VendaDTO dto) {
        Venda venda = new Venda();
        venda.setIdTransacao(dto.getIdTransacao());

        try {
            venda.setDataVenda(LocalDate.parse(dto.getDataVenda()));
        } catch (DateTimeParseException e) {
            venda.setDataVenda(LocalDate.parse(dto.getDataVenda(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }

        venda.setValorFinal(BigDecimal.valueOf(dto.getValorFinal()));
        venda.setSubtotal(dto.getSubtotal() != null ? BigDecimal.valueOf(dto.getSubtotal()) : BigDecimal.ZERO);
        venda.setDescontoPercent(dto.getDescontoPercent());
        venda.setCanalVenda(parseEnum(CanalVenda.class, dto.getCanalVenda()));
        venda.setFormaPagamento(parseEnum(FormaPagamento.class, dto.getFormaPagamento()));

        // Cliente
        Cliente cliente = new Cliente();
        cliente.setClienteId(dto.getClienteId());
        cliente.setNomeCliente(dto.getNomeCliente());
        cliente.setIdade(dto.getIdadeCliente());
        cliente.setGenero(parseEnum(Genero.class, dto.getGeneroCliente()));
        cliente.setCidade(dto.getCidadeCliente());
        cliente.setEstado(dto.getEstadoCliente());
        if(dto.getRendaEstimada() != null)
            cliente.setRendaEstimada(BigDecimal.valueOf(dto.getRendaEstimada()));
        venda.setCliente(cliente);

        // Produto
        Produto produto = new Produto();
        produto.setProdutoId(dto.getProdutoId());
        produto.setNomeProduto(dto.getNomeProduto());
        produto.setCategoria(dto.getCategoria());
        produto.setMarca(dto.getMarca());
        if(dto.getPrecoUnitario() != null)
            produto.setPrecoUnitario(BigDecimal.valueOf(dto.getPrecoUnitario()));
        produto.setQuantidade(dto.getQuantidade());
        produto.setMargemLucro(dto.getMargemLucro());
        venda.setProduto(produto);

        // Logistica
        Logistica logistica = new Logistica();
        logistica.setRegiao(parseEnum(Regiao.class, dto.getRegiao()));
        // Trata "Em Trânsito" virando "EM_TRANSITO"
        if(dto.getStatusEntrega() != null) {
            String statusNorm = dto.getStatusEntrega().trim().toUpperCase().replace(" ", "_");
            try {
                logistica.setStatusEntrega(StatusEntrega.valueOf(statusNorm));
            } catch (Exception e) { /* Deixa nulo se falhar */ }
        }
        logistica.setTempoEntregaDias(dto.getTempoEntregaDias());
        logistica.setVendedorId(dto.getVendedorId());
        venda.setLogistica(logistica);

        return venda;
    }

    // UTILITÁRIOS

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Map<String, Integer> mapearCabecalho(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].trim().toLowerCase(), i);
        }
        return map;
    }

    private Map<String, Integer> mapearCabecalhoExcel(Row row) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : row) {
            map.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
        }
        return map;
    }
}