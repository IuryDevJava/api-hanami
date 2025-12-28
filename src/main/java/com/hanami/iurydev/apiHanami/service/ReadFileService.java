package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.dto.VendaDTO;
import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.entity.embeddable.Cliente;
import com.hanami.iurydev.apiHanami.entity.embeddable.Logistica;
import com.hanami.iurydev.apiHanami.entity.embeddable.Produto;
import com.hanami.iurydev.apiHanami.entity.enums.*;
import com.hanami.iurydev.apiHanami.repository.VendaRepository;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
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
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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
            throw new IllegalArgumentException("Formato de arquivo não suportado");
        }

        return processarEConverter(dtos);
    }

    private List<VendaDTO> readCSV(MultipartFile file) throws IOException {
        List<VendaDTO> dtos = new ArrayList<>();
        var parser = new CSVParserBuilder().withSeparator(',').build();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parser).build()) {

            String[] headers = csvReader.readNext();
            if (headers == null) return dtos;

            Map<String, Integer> headerMap = mapearCabecalho(headers);
            verificarColunasObrigatorias(headerMap);

            String[] row;
            while ((row = csvReader.readNext()) != null) {
                String[] finalRow = row;
                dtos.add(extrairDadosDaLinha(headerMap, i -> i < finalRow.length ? finalRow[i] : null));
            }
        } catch (CsvValidationException e) {
            throw new IOException("Falha ao validar estrutura do CSV", e);
        }
        return dtos;
    }

    private List<VendaDTO> readExcel(MultipartFile file) throws IOException {
        List<VendaDTO> dtos = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) return dtos;

            Map<String, Integer> headerMap = mapearCabecalhoExcel(rowIterator.next());
            verificarColunasObrigatorias(headerMap);
            DataFormatter formatter = new DataFormatter();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                dtos.add(extrairDadosDaLinha(headerMap, i -> formatter.formatCellValue(row.getCell(i))));
            }
        }
        return dtos;
    }

    private VendaDTO extrairDadosDaLinha(Map<String, Integer> map, java.util.function.Function<Integer, String> getVal) {
        VendaDTO dto = new VendaDTO();
        dto.setIdTransacao(getValByHeader(map, getVal, "id_transacao"));
        dto.setDataVenda(getValByHeader(map, getVal, "data_venda"));
        dto.setValorFinal(parseDeDouble(getValByHeader(map, getVal, "valor_final")));
        dto.setSubtotal(parseDeDouble(getValByHeader(map, getVal, "subtotal")));
        dto.setDescontoPercent(parseDeDouble(getValByHeader(map, getVal, "desconto_percent")));
        dto.setCanalVenda(getValByHeader(map, getVal, "canal_venda"));
        dto.setFormaPagamento(getValByHeader(map, getVal, "forma_pagamento"));
        dto.setClienteId(getValByHeader(map, getVal, "cliente_id"));
        dto.setNomeCliente(getValByHeader(map, getVal, "nome_cliente"));
        dto.setIdadeCliente(parseDeInteger(getValByHeader(map, getVal, "idade_cliente")));
        dto.setGeneroCliente(getValByHeader(map, getVal, "genero_cliente"));
        dto.setCidadeCliente(getValByHeader(map, getVal, "cidade_cliente"));
        dto.setEstadoCliente(getValByHeader(map, getVal, "estado_cliente"));
        dto.setRendaEstimada(parseDeDouble(getValByHeader(map, getVal, "renda_estimada")));
        dto.setProdutoId(getValByHeader(map, getVal, "produto_id"));
        dto.setNomeProduto(getValByHeader(map, getVal, "nome_produto"));
        dto.setCategoria(getValByHeader(map, getVal, "categoria"));
        dto.setMarca(getValByHeader(map, getVal, "marca"));
        dto.setPrecoUnitario(parseDeDouble(getValByHeader(map, getVal, "preco_unitario")));
        dto.setQuantidade(parseDeInteger(getValByHeader(map, getVal, "quantidade")));
        dto.setMargemLucro(parseDeDouble(getValByHeader(map, getVal, "margem_lucro")));
        dto.setRegiao(getValByHeader(map, getVal, "regiao"));
        dto.setStatusEntrega(getValByHeader(map, getVal, "status_entrega"));
        dto.setTempoEntregaDias(parseDeInteger(getValByHeader(map, getVal, "tempo_entrega_dias")));
        dto.setVendedorId(getValByHeader(map, getVal, "vendedor_id"));
        return dto;
    }

    private List<Venda> processarEConverter(List<VendaDTO> dtos) {
        Map<String, Venda> mapaVendas = new LinkedHashMap<>();

        for (VendaDTO dto : dtos) {
            Set<ConstraintViolation<VendaDTO>> violacoes = validator.validate(dto);

            try {
                Venda venda = convertDtoToEntity(dto);

                if (violacoes.isEmpty()) {
                    venda.setProcessadoSucesso(true);
                    venda.setObservacaoValidada("OK");
                } else {
                    venda.setProcessadoSucesso(false);
                    String erros = violacoes.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(" | "));
                    venda.setObservacaoValidada(erros);
                }

                mapaVendas.put(venda.getIdTransacao(), venda);
            } catch (Exception e) {
                System.err.println("Erro crítico na linha: " + dto.getIdTransacao());
            }
        }

        if (mapaVendas.isEmpty()) return new ArrayList<>();

        List<String> idsExistentes = vendaRepository.findExistingIds(new ArrayList<>(mapaVendas.keySet()));
        idsExistentes.forEach(mapaVendas::remove);

        return vendaRepository.saveAll(mapaVendas.values());
    }

    private Venda convertDtoToEntity(VendaDTO dto) {
        Venda v = new Venda();
        v.setIdTransacao(dto.getIdTransacao());
        v.setDataVenda(parseData(dto.getDataVenda()));
        v.setValorFinal(BigDecimal.valueOf(dto.getValorFinal()));
        v.setSubtotal(dto.getSubtotal() != null ? BigDecimal.valueOf(dto.getSubtotal()) : BigDecimal.ZERO);
        v.setDescontoPercent(dto.getDescontoPercent());
        v.setCanalVenda(parseEnum(CanalVenda.class, dto.getCanalVenda()));
        v.setFormaPagamento(parseEnum(FormaPagamento.class, dto.getFormaPagamento()));

        Cliente c = new Cliente();
        c.setClienteId(dto.getClienteId());
        c.setNomeCliente(dto.getNomeCliente());
        c.setIdade(dto.getIdadeCliente());
        c.setGenero(parseEnum(Genero.class, dto.getGeneroCliente()));
        c.setCidade(dto.getCidadeCliente());
        c.setEstado(dto.getEstadoCliente());
        if (dto.getRendaEstimada() != null) c.setRendaEstimada(BigDecimal.valueOf(dto.getRendaEstimada()));
        v.setCliente(c);

        Produto p = new Produto();
        p.setProdutoId(dto.getProdutoId());
        p.setNomeProduto(dto.getNomeProduto());
        p.setCategoria(dto.getCategoria());
        p.setMarca(dto.getMarca());
        if (dto.getPrecoUnitario() != null) p.setPrecoUnitario(BigDecimal.valueOf(dto.getPrecoUnitario()));
        p.setQuantidade(dto.getQuantidade());
        p.setMargemLucro(dto.getMargemLucro());
        v.setProduto(p);

        Logistica l = new Logistica();
        l.setRegiao(parseEnum(Regiao.class, dto.getRegiao()));
        l.setStatusEntrega(parseEnum(StatusEntrega.class, dto.getStatusEntrega()));
        l.setTempoEntregaDias(dto.getTempoEntregaDias());
        l.setVendedorId(dto.getVendedorId());
        v.setLogistica(l);

        return v;
    }

    private LocalDate parseData(String data) {
        try {
            return LocalDate.parse(data);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    private Double parseDeDouble(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Double.parseDouble(v.replace("R$", "").replace(".", "").replace(",", ".").trim());
        } catch (Exception e) { return null; }
    }

    private Integer parseDeInteger(String v) {
        Double d = parseDeDouble(v);
        return d != null ? d.intValue() : null;
    }

    private String getValByHeader(Map<String, Integer> map, java.util.function.Function<Integer, String> getVal, String key) {
        Integer idx = map.get(key);
        return idx != null ? getVal.apply(idx) : null;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) return null;
        String norm = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .trim().toUpperCase().replace(" ", "_").replace("-", "_");
        try { return Enum.valueOf(enumClass, norm); } catch (Exception e) { return null; }
    }

    private Map<String, Integer> mapearCabecalho(String[] headers) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            map.put(headers[i].replace("\uFEFF", "").trim().toLowerCase(), i);
        }
        return map;
    }

    private Map<String, Integer> mapearCabecalhoExcel(Row row) {
        Map<String, Integer> map = new HashMap<>();
        row.forEach(cell -> map.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex()));
        return map;
    }

    private void mapearCabecalho(Row headerRow) {
        List<String> colunasEsperadas = Arrays.asList("ID Transação", "Data", "Região", "Estado", "Vendedor", "Item", "Unidades", "Preço Unitário", "Custo Unitário");

        for (String coluna : colunasEsperadas) {
            boolean encontrada = false;
            for (Cell cell : headerRow) {
                if (cell.getStringCellValue().equalsIgnoreCase(coluna)) {
                    encontrada = true;
                    break;
                }
            }
            if (!encontrada) {
                throw new IllegalArgumentException("Coluna ausente: " + coluna);
            }
        }
    }

    private void verificarColunasObrigatorias(Map<String, Integer> headerMap) {
        List<String> obrigatorias = Arrays.asList("id_transacao", "data_venda", "valor_final", "margem_lucro");

        for (String coluna : obrigatorias) {
            if (!headerMap.containsKey(coluna)) {
                throw new IllegalArgumentException("Coluna obrigatória ausente: " + coluna);
            }
        }
    }
}