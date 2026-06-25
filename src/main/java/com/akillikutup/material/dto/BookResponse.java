package com.akillikutup.material.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private String id;
    private String baslik;
    private double birimFiyat;
    private int stokAdedi;
    private String tur;
    private String yazar;
    private String kategori;
    private String isbn;
    private String kapakGorseli;
    private String dosyaFormati;
    private String dijitalTur;
    private String boyut;
    private String message;
}
