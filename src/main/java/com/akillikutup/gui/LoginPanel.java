package com.akillikutup.gui;

import com.akillikutup.core.Kullanici;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginPanel extends JPanel {
    private MainFrame mainFrame;
    private JComboBox<String> userComboBox;
    private JPasswordField passwordField;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Akilli-Kutup Giris");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        JLabel selectUserLabel = new JLabel("Kullanici Secin:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(selectUserLabel, gbc);

        userComboBox = new JComboBox<>();
        List<Kullanici> users = LibraryManager.getInstance().getUsers();
        for (Kullanici user : users) {
            userComboBox.addItem(user.getIsim());
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(userComboBox, gbc);

        JLabel passwordLabel = new JLabel("Sifre:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(passwordField, gbc);

        JButton loginButton = new JButton("Giris Yap");
        loginButton.addActionListener(e -> {
            int selectedIndex = userComboBox.getSelectedIndex();
            if (selectedIndex >= 0) {
                Kullanici selectedUser = users.get(selectedIndex);
                String enteredPassword = new String(passwordField.getPassword());
                if (selectedUser.getSifre().equals(enteredPassword)) {
                    LibraryManager.getInstance().setCurrentUser(selectedUser);
                    passwordField.setText("");
                    if (selectedUser.getRol().equals("ADMIN")) {
                        mainFrame.showPanel("ADMIN");
                    } else {
                        mainFrame.showPanel("USER");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Hatali sifre!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(loginButton, gbc);
    }
}
