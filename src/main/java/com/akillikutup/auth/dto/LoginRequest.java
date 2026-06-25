package com.akillikutup.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String tcKimlikNo;
    private String sifre;
    private String sifreHash;
    private String totpCode;
}
