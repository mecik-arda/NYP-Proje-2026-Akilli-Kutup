package com.akillikutup;

import com.akillikutup.core.*;

public class Main {
    public static void main(String[] args) {
        // Kullanicilari olustur
        Admin adminAhmet = new Admin("Ahmet Guler", "11122233344");
        Uye uyeMehmet = new Uye("Mehmet Yilmaz", "99988877766");

        System.out.println("--- GUVENLIK VE ERISIM TESTI ---");

        // 1. Durum: Admin, bir Uyenin TC'sine erismek istiyor (IZINLI)
        System.out.println("Admin, Mehmet'in TC'sini sorguluyor: " +
                uyeMehmet.getTcNo(adminAhmet));

        // 2. Durum: Uye, kendi TC'sine erismek istiyor (IZINLI)
        System.out.println("Mehmet kendi TC'sini sorguluyor: " +
                uyeMehmet.getTcNo(uyeMehmet));

        // 3. Durum: Uye (Mehmet), Admin'in (Ahmet) TC'sine erismek istiyor (YETKI YOK)
        System.out.println("Mehmet, Admin Ahmet'in TC'sini sorguluyor: " +
                adminAhmet.getTcNo(uyeMehmet));

        System.out.println("---------------------------------");
    }
}