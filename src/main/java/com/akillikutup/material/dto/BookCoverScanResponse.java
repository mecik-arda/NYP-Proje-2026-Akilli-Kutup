package com.akillikutup.material.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BookCoverScanResponse {
    @JsonProperty("basarili")
    private boolean basarili;
    private Object sonuc;
    private String mesaj;
}
