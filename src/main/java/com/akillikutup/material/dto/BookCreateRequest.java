package com.akillikutup.material.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {
    private String baslik;
    private String yazar;
    private String kategori;
    private int stokAdedi;
    private double birimFiyat;
    private String isbn;
    private String kapakGorseliBase64;
    private String kapakGorseliAdi;
}
