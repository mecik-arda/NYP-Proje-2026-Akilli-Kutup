package com.akillikutup;

import com.akillikutup.config.ConfigManager;
import com.akillikutup.gui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        ConfigManager.init();

        java.util.List<com.akillikutup.user.User> users = com.akillikutup.db.DatabaseManager.tekOrnekAl().getKullaniciListesi();
        java.util.List<com.akillikutup.material.Materyal> materials = com.akillikutup.db.DatabaseManager.tekOrnekAl().getMateryalListesi();
        if (users.isEmpty() && com.akillikutup.db.DatabaseManager.isTestMode) {
            com.akillikutup.auth.AuthManager auth = new com.akillikutup.auth.AuthManager();
            users.add(new com.akillikutup.user.User("Ahmet Guler", "11111111111",
                com.akillikutup.user.User.Role.ADMIN, auth.registerPassword("12345678")));
            users.add(new com.akillikutup.user.User("Arda Mecik", "22222222222",
                com.akillikutup.user.User.Role.ADMIN, auth.registerPassword("12345678")));
            users.add(new com.akillikutup.user.User("Eren Gider", "33333333333",
                com.akillikutup.user.User.Role.ADMIN, auth.registerPassword("12345678")));
            users.add(new com.akillikutup.user.User("Goktug Berke Kuzucu", "44444444444",
                com.akillikutup.user.User.Role.UYE, auth.registerPassword("12345678")));
            com.akillikutup.db.DatabaseManager.tekOrnekAl().kaydet(users, materials);
        }
        if (materials.isEmpty() && com.akillikutup.db.DatabaseManager.isTestMode) {
            materials.add(new com.akillikutup.material.Kitap("1984", 5, 25.0, "978-0451524935"));
            materials.add(new com.akillikutup.material.Kitap("Seker Portakali", 3, 15.0, "978-9750719387"));
            materials.add(new com.akillikutup.material.Kitap("Kucuk Prens", 7, 20.0, "978-9750726439"));
            materials.add(new com.akillikutup.material.Kitap("Suç ve Ceza", 4, 30.0, "978-9754580662"));
            materials.add(new com.akillikutup.material.Kitap("Simyaci", 6, 22.0, "978-9750726446"));
            materials.add(new com.akillikutup.material.DijitalMedya("Inception (Film)", 10.0, "MP4", "Video", "1 GB"));
            materials.add(new com.akillikutup.material.DijitalMedya("Interstellar (Film)", 12.0, "MP4", "Video", "1.5 GB"));
            materials.add(new com.akillikutup.material.DijitalMedya("Java Dersleri (Video)", 0.0, "MP4", "Video", "500 MB"));
            com.akillikutup.db.DatabaseManager.tekOrnekAl().kaydet(users, materials);
        }

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
