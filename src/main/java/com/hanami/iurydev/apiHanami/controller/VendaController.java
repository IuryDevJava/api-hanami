package com.hanami.iurydev.apiHanami.controller;

import com.hanami.iurydev.apiHanami.dto.UploadDTO;
import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.service.ReadFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final ReadFileService readFileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadDTO> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UploadDTO("erro", 0));
        }

        try {
            List<Venda> processados = readFileService.readFile(file);

            if (processados.isEmpty()) {
                return ResponseEntity.ok(new UploadDTO("Aviso: Nenhuma nova linha processada", 0));
            }

            // Retorna 200
            return ResponseEntity.ok(new UploadDTO("sucesso", processados.size()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new UploadDTO(e.getMessage(), 0));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadDTO("Erro interno ao processar o arquivo", 0));
        }
    }
}