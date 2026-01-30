package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadDTO {

    @Schema(example = "sucesso")
    private String status;

    @Schema(example = "10000")
    @JsonProperty("linhas_processadas")
    private Integer linhasProcessadas;
}
