package com.akillikutup.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("Akilli-Kutup Sistemi");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        LoginPanel loginPanel = new LoginPanel(this);
        AdminPanel adminPanel = new AdminPanel(this);
        UserPanel userPanel = new UserPanel(this);

        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(adminPanel, "ADMIN");
        mainPanel.add(userPanel, "USER");

        add(mainPanel);
    }

    public void showPanel(String panelName) {
        if (panelName.equals("ADMIN")) {
            Component[] components = mainPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof AdminPanel) {
                    ((AdminPanel) comp).refreshData();
                }
            }
        } else if (panelName.equals("USER")) {
            Component[] components = mainPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof UserPanel) {
                    ((UserPanel) comp).refreshData();
                }
            }
        }
        cardLayout.show(mainPanel, panelName);
    }
}