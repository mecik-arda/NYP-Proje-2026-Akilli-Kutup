package com.akillikutup.user.dto;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String isim;
    private String tcKimlikNo;
    private String email;
    private String rol;
    private String sifre;
}
