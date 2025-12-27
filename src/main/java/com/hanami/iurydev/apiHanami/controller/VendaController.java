package com.hanami.iurydev.apiHanami.controller;

import com.hanami.iurydev.apiHanami.dto.UploadDTO;
import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.service.ReadFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final ReadFileService readFileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadDTO("Erro: Arquivo não enviado", 0));
        }

        try {
            List<Venda> processados = readFileService.readFile(file);

            if (processados.isEmpty()) {
                return ResponseEntity.ok(new UploadDTO("Aviso: Nenhuma nova linha válida processada (verifique duplicatas ou regras de negócio)", 0));
            }

            return ResponseEntity.ok(new UploadDTO("sucesso", processados.size()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new UploadDTO(e.getMessage(), 0));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new UploadDTO("Erro interno no servidor", 0));
        }
    }

}
