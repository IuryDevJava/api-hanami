package com.hanami.iurydev.apiHanami.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadDTO {

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Linhas_processadas")
    private Integer linhasProcessadas;
}
