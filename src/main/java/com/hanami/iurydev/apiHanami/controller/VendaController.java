package com.hanami.iurydev.apiHanami.controller;

import com.hanami.iurydev.apiHanami.dto.UploadDTO;
import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.service.ReadFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
@Slf4j
public class VendaController {

    private final ReadFileService readFileService;

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
}