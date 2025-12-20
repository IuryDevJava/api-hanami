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

        try {
            List<Venda> salesSave = readFileService.readFile(file);

            UploadDTO response = new UploadDTO(
                    "sucesso",
                    salesSave.size()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new UploadDTO("Erro: " + e.getMessage(), 0));
        }

    }

}
