package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.entity.Venda;
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

    // Constantes para evitar "Magic Numbers" e facilitar manutenção
    private static final int INDEX_ID_TRANSACAO = 0;
    private static final int INDEX_DATA_VENDA = 1;
    private static final int INDEX_VALOR_FINAL = 2;

    public List<Venda> readFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();

        if (filename != null && filename.endsWith(".csv")) {
            return readCSV(file);
        } else if (filename != null && filename.endsWith(".xlsx")) {
            return readExcel(file);
        } else {
            throw new IllegalArgumentException("Formato inválido. Utilize .csv ou .xlsx");
        }
    }

    private List<Venda> readCSV(MultipartFile file) throws IOException {
        List<Venda> vendas = new ArrayList<>();

        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[] line;
            csvReader.readNext();

            // Economiza memória em arquivos grandes.
            while ((line = csvReader.readNext()) != null) {
                // Validação
                if (line.length < 3) continue;

                Venda venda = convertDataToVenda(line);
                if (venda != null) {
                    vendas.add(venda);
                }
            }
        } catch (CsvValidationException e) {
            throw new IOException("Erro na validação do CSV", e);
        }

        return vendas;
    }

    private List<Venda> readExcel(MultipartFile file) throws IOException {
        List<Venda> vendas = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue; // Pula cabeçalho

                String[] data = convertRowExcelToArray(row);

                Venda venda = convertDataToVenda(data);
                if (venda != null) {
                    vendas.add(venda);
                }
            }
        }
        return vendas;
    }

    private String[] convertRowExcelToArray(Row row) {
        int numColumns = 25;
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

            venda.setIdTransacao(cleanText(data[INDEX_ID_TRANSACAO]));

            if (hasValue(data[INDEX_DATA_VENDA])) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                venda.setDataVenda(LocalDate.parse(data[INDEX_DATA_VENDA].trim(), formatter));
            }

            // Valor
            if (hasValue(data[INDEX_VALOR_FINAL])) {
                String valorLimpo = data[INDEX_VALOR_FINAL].trim().replace("R$", "").replace(",", ".");
                venda.setValorFinal(new BigDecimal(valorLimpo));
            }

            return venda;

        } catch (Exception e) {
            System.err.println("Erro ao converter linha ID " + (data.length > 0 ? data[0] : "?") + ": " + e.getMessage());
            return null;
        }
    }

    private boolean hasValue(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private String cleanText(String text) {
        return text != null ? text.trim() : "";
    }
}