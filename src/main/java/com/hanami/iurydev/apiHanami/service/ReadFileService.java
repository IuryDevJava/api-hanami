package com.hanami.iurydev.apiHanami.service;

import com.hanami.iurydev.apiHanami.entity.Venda;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadFileService {

    public List<Venda> readFile(MultipartFile file) throws IOException {
        String nameFile = file.getOriginalFilename();

        if (nameFile != null && nameFile.endsWith(".csv")) {
            return readCSV(file);
        } else if (nameFile != null && nameFile.endsWith(".xlsx")) {
            return readExcel(file);
        } else {
            throw new IllegalArgumentException("Formato não suportado. Envie com extensão .csv ou .xlsx");
        }
    }

    private List<Venda> readCSV(MultipartFile file) throws IOException {
        List<Venda> lists = new ArrayList<>();

        return lists;
    }

    private List<Venda> readExcel(MultipartFile file) throws IOException {
        List<Venda> lists = new ArrayList<>();

        return lists;
    }
}
