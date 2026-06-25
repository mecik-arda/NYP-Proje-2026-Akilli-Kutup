package com.akillikutup.user.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String isim;
    private String tcKimlikNo;
    private String email;
}
