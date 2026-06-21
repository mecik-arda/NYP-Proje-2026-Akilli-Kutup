package com.akillikutup;

import com.akillikutup.core.ConfigManager;
import com.akillikutup.gui.MainFrame;
import com.akillikutup.server.ApiServer;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        ConfigManager.init();
        
        java.util.List<com.akillikutup.core.Kullanici> users = com.akillikutup.db.DatabaseManager.tekOrnekAl().getKullaniciListesi();
        java.util.List<com.akillikutup.core.Materyal> materials = com.akillikutup.db.DatabaseManager.tekOrnekAl().getMateryalListesi();
        if (users.isEmpty() && com.akillikutup.db.DatabaseManager.isTestMode) {
            com.akillikutup.auth.AuthManager auth = new com.akillikutup.auth.AuthManager();
            users.add(new com.akillikutup.core.Admin("Ahmet Guler", "11111111111", auth.registerPassword("12345678")));
            users.add(new com.akillikutup.core.Admin("Arda Mecik", "22222222222", auth.registerPassword("12345678")));
            users.add(new com.akillikutup.core.Admin("Eren Gider", "33333333333", auth.registerPassword("12345678")));
            users.add(new com.akillikutup.core.Uye("Goktug Berke Kuzucu", "44444444444", auth.registerPassword("12345678")));
            com.akillikutup.db.DatabaseManager.tekOrnekAl().kaydet(users, materials);
        }
        if (materials.isEmpty() && com.akillikutup.db.DatabaseManager.isTestMode) {
            materials.add(new com.akillikutup.core.Kitap("1984", 5, 25.0, "978-0451524935"));
            materials.add(new com.akillikutup.core.Kitap("Seker Portakali", 3, 15.0, "978-9750719387"));
            materials.add(new com.akillikutup.core.Kitap("Kucuk Prens", 7, 20.0, "978-9750726439"));
            materials.add(new com.akillikutup.core.Kitap("Suç ve Ceza", 4, 30.0, "978-9754580662"));
            materials.add(new com.akillikutup.core.Kitap("Simyaci", 6, 22.0, "978-9750726446"));
            materials.add(new com.akillikutup.core.DijitalMedya("Inception (Film)", 10.0, "MP4", "Video", "1 GB"));
            materials.add(new com.akillikutup.core.DijitalMedya("Interstellar (Film)", 12.0, "MP4", "Video", "1.5 GB"));
            materials.add(new com.akillikutup.core.DijitalMedya("Java Dersleri (Video)", 0.0, "MP4", "Video", "500 MB"));
            com.akillikutup.db.DatabaseManager.tekOrnekAl().kaydet(users, materials);
        }
        
        // Eski ApiServer kaldirildi (Artik Spring Boot kullaniliyor)
        System.out.println("Sadece Swing Masaustu Arayuzu baslatiliyor...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            } catch (Exception e) {
                System.out.println("GUI baslatilamadigi icin sadece API sunucusu calismaya devam ediyor.");
                e.printStackTrace();
            }
        });
        
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Sunucu sonlandirildi.");
        }
    }
}