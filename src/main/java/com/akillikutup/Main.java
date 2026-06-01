package com.akillikutup;

import com.akillikutup.core.ConfigManager;
import com.akillikutup.gui.MainFrame;
import com.akillikutup.server.ApiServer;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        ConfigManager.init();
        
        ApiServer apiServer = new ApiServer();
        apiServer.startServer(8080);
        
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Sistem basariyla headless modda baslatildi. GUI devre disi birakildi.");
            System.out.println("API Sunucusu http://localhost:8080 adresinde aktif.");
            try {
                Object lock = new Object();
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                System.out.println("Sunucu sonlandirildi.");
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                } catch (Exception e) {
                    System.out.println("GUI baslatilamadigi icin sadece API sunucusu calismaya devam ediyor.");
                }
            });
        }
    }
}