package com.akillikutup;

import com.akillikutup.core.ConfigManager;
import com.akillikutup.gui.MainFrame;
import com.akillikutup.server.ApiServer;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        ConfigManager.init();
        
        ApiServer apiServer = new ApiServer();
        apiServer.startServer(8080);
        
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}