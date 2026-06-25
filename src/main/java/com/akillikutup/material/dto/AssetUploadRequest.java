package com.akillikutup.material.dto;

import lombok.Data;

@Data
public class AssetUploadRequest {
    private String baslik;
    private String tur;
    private String boyut;
    private String format;
}
