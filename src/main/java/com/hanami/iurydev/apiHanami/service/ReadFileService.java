package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.entity.embeddable.Cliente;
import com.hanami.iurydev.apiHanami.entity.enums.CanalVenda;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadFileService {

    private static final String[] EXPECTED_HEADERS = {
            "id_transacao", "data_venda", "valor_final", "canal_venda", "idade_cliente"
    };

    private static final int IDX_ID_TRANSACAO = 0;
    private static final int IDX_DATA_VENDA = 1;
    private static final int IDX_VALOR_FINAL = 2;
    private static final int IDX_CANAL_VENDA = 3;
    private static final int IDX_IDADE_CLIENTE = 4;

    public List<Venda> readFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName != null && fileName.endsWith(".csv")) {
            return readCSV(file);
        } else if (fileName != null && fileName.endsWith(".xlsx")) {
            return readExcel(file);
        } else {
            throw new IllegalArgumentException("Formato inválido. Por favor, envie um arquivo .csv ou .xlsx");
        }
    }

    private List<Venda> readCSV(MultipartFile file) throws IOException {
        List<Venda> salesList = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] header = csvReader.readNext();
            validateHeaders(header);

            String[] rowArray;
            while ((rowArray = csvReader.readNext()) != null) {
                if (rowArray.length < EXPECTED_HEADERS.length) continue;

                Venda venda = convertDataToVenda(rowArray);
                processAndAddVenda(salesList, venda);
            }

        } catch (CsvValidationException e) {
            throw new IOException("Erro ao validar a estrutura do CSV", e);
        }

        return salesList;
    }

    private List<Venda> readExcel(MultipartFile file) throws IOException {
        List<Venda> salesList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(0);
            String[] headerArray = convertRowExcelToArray(headerRow);
            validateHeaders(headerArray);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String[] rowArray = convertRowExcelToArray(row);

                Venda venda = convertDataToVenda(rowArray);
                processAndAddVenda(salesList, venda);
            }
        }
        return salesList;
    }

    private void processAndAddVenda(List<Venda> salesList, Venda venda) {
        if (venda != null) {
            Venda validatedVenda = validateAndSanitize(venda);
            if (validatedVenda != null) {
                salesList.add(validatedVenda);
            }
        }
    }

    private void validateHeaders(String[] headers) {
        if (headers == null || headers.length < EXPECTED_HEADERS.length) {
            throw new IllegalArgumentException("Arquivo inválido: Colunas obrigatórias ausentes.");
        }

        for (int i = 0; i < EXPECTED_HEADERS.length; i++) {
            String fileHeader = headers[i].trim();
            String expectedHeader = EXPECTED_HEADERS[i];

            if (!fileHeader.equalsIgnoreCase(expectedHeader)) {
                throw new IllegalArgumentException(
                        String.format("Erro de Cabeçalho: Esperado '%s', encontrado '%s' na coluna %d.",
                                expectedHeader, fileHeader, i + 1)
                );
            }
        }
    }

    private String[] convertRowExcelToArray(Row row) {
        if (row == null) return new String[0];

        int numColumns = Math.max(EXPECTED_HEADERS.length, row.getLastCellNum());
        String[] data = new String[numColumns];
        DataFormatter dataFormatter = new DataFormatter();

        for (int i = 0; i < numColumns; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            data[i] = dataFormatter.formatCellValue(cell);
        }
        return data;
    }

    private Venda convertDataToVenda(String[] data) {
        try {
            Venda venda = new Venda();

            venda.setIdTransacao(cleanText(data[IDX_ID_TRANSACAO]));

            if (hasValue(data[IDX_DATA_VENDA])) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                venda.setDataVenda(LocalDate.parse(data[IDX_DATA_VENDA].trim(), formatter));
            }

            if (hasValue(data[IDX_VALOR_FINAL])) {
                String cleanValue = data[IDX_VALOR_FINAL].trim().replace("R$", "").replace(",", ".");
                venda.setValorFinal(new BigDecimal(cleanValue));
            }

            if (hasValue(data[IDX_IDADE_CLIENTE])) {
                try {
                    int age = Integer.parseInt(data[IDX_IDADE_CLIENTE].trim());
                    if (venda.getCliente() == null) venda.setCliente(new Cliente());
                    venda.getCliente().setIdade(age);
                } catch (NumberFormatException e) {
                    System.err.println("Aviso: Formato de idade inválido para o ID " + venda.getIdTransacao());
                }
            }

            if (hasValue(data[IDX_CANAL_VENDA])) {
                String normalizedChannel = data[IDX_CANAL_VENDA].trim().toUpperCase();
                try {
                    venda.setCanalVenda(CanalVenda.valueOf(normalizedChannel));
                } catch (IllegalArgumentException e) {
                    System.err.println("Aviso: Canal de venda desconhecido: " + normalizedChannel);
                }
            }

            return venda;

        } catch (Exception e) {
            String idRef = (data != null && data.length > 0) ? data[0] : "Desconhecido";
            System.err.println("Erro crítico ao processar linha ID " + idRef + ": " + e.getMessage());
            return null;
        }
    }

    private Venda validateAndSanitize(Venda venda) {
        if (!hasValue(venda.getIdTransacao())) {
            System.err.println("Registro descartado: ID da Transação ausente.");
            return null;
        }

        if (venda.getValorFinal() == null) {
            System.err.println("Registro descartado ID " + venda.getIdTransacao() + ": Valor Final ausente.");
            return null;
        }

        if (venda.getDataVenda() == null) {
            System.err.println("Registro descartado ID " + venda.getIdTransacao() + ": Data ausente.");
            return null;
        }

        if (venda.getDescontoPercent() == null) {
            venda.setDescontoPercent(0.0);
        }

        if (venda.getSubtotal() == null) {
            venda.setSubtotal(venda.getValorFinal());
        }

        return venda;
    }


    private boolean hasValue(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private String cleanText(String text) {
        return text != null ? text.trim() : "";
    }
}