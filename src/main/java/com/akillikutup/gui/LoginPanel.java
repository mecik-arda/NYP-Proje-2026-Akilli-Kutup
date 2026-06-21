package com.akillikutup.gui;

import com.akillikutup.core.Kullanici;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField tcField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("Akilli-Kutup Giris (V4 REST API)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        JLabel tcLabel = new JLabel("TC Kimlik No:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(tcLabel, gbc);

        tcField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        add(tcField, gbc);

        JLabel passwordLabel = new JLabel("Sifre:");
        gbc.gridx = 0; gbc.gridy = 2;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        add(passwordField, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        add(statusLabel, gbc);

        JButton loginButton = new JButton("Giris Yap (REST API)");
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> performLogin());
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(loginButton, gbc);

        JButton registerButton = new JButton("Kayit Ol");
        registerButton.addActionListener(e -> showRegisterDialog());
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(registerButton, gbc);

        JButton checkServerButton = new JButton("Sunucu Kontrolu");
        checkServerButton.addActionListener(e -> {
            boolean alive = LibraryManager.getInstance().isServerAlive();
            if (alive) {
                statusLabel.setText("✅ Sunucu aktif: http://localhost:8080");
                statusLabel.setForeground(new Color(0, 128, 0));
            } else {
                statusLabel.setText("❌ Sunucuya ulasilamiyor! Spring Boot calisiyor mu?");
                statusLabel.setForeground(Color.RED);
            }
        });
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        add(checkServerButton, gbc);
    }

    private void performLogin() {
        String tcNo = tcField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (tcNo.isEmpty() || password.isEmpty()) {
            statusLabel.setText("TC No ve sifre gerekli!");
            statusLabel.setForeground(Color.RED);
            return;
        }

        if (tcNo.length() != 11) {
            statusLabel.setText("TC Kimlik No 11 haneli olmalidir!");
            statusLabel.setForeground(Color.RED);
            return;
        }

        statusLabel.setText("🔄 REST API'ye baglaniliyor...");
        statusLabel.setForeground(Color.BLUE);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return LibraryManager.getInstance().login(tcNo, password);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        statusLabel.setText("✅ Giris basarili! JWT token alindi.");
                        statusLabel.setForeground(new Color(0, 128, 0));
                        passwordField.setText("");

                        Kullanici user = LibraryManager.getInstance().getCurrentUser();
                        if (user != null && "ADMIN".equals(user.getRol())) {
                            mainFrame.showPanel("ADMIN");
                        } else {
                            mainFrame.showPanel("USER");
                        }
                    } else {
                        statusLabel.setText("❌ Hatali TC No veya sifre!");
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    statusLabel.setText("❌ Sunucu hatasi: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void showRegisterDialog() {
        JDialog registerDialog = new JDialog(mainFrame, "Yeni Kullanici Kaydi", true);
        registerDialog.setSize(420, 350);
        registerDialog.setLayout(new GridBagLayout());
        registerDialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel rolLabel = new JLabel("Rol:");
        gbc.gridx = 0; gbc.gridy = 0;
        registerDialog.add(rolLabel, gbc);
        JComboBox<String> rolCombo = new JComboBox<>(new String[]{"Uye", "Admin"});
        gbc.gridx = 1; gbc.gridy = 0;
        registerDialog.add(rolCombo, gbc);

        JLabel nameLabel = new JLabel("Ad Soyad:");
        gbc.gridx = 0; gbc.gridy = 1;
        registerDialog.add(nameLabel, gbc);
        JTextField nameField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        registerDialog.add(nameField, gbc);

        JLabel tcLabel = new JLabel("TC Kimlik No:");
        gbc.gridx = 0; gbc.gridy = 2;
        registerDialog.add(tcLabel, gbc);
        JTextField tcField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        registerDialog.add(tcField, gbc);

        JLabel emailLabel = new JLabel("E-posta:");
        gbc.gridx = 0; gbc.gridy = 3;
        registerDialog.add(emailLabel, gbc);
        JTextField emailField = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 3;
        registerDialog.add(emailField, gbc);

        JLabel passLabel = new JLabel("Sifre:");
        gbc.gridx = 0; gbc.gridy = 4;
        registerDialog.add(passLabel, gbc);
        JPasswordField passField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 4;
        registerDialog.add(passField, gbc);

        JLabel statusRegLabel = new JLabel(" ");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        registerDialog.add(statusRegLabel, gbc);

        JButton saveButton = new JButton("Kaydet ve Kayit Ol (REST API)");
        saveButton.addActionListener(event -> {
            String name = nameField.getText().trim();
            String tc = tcField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String secilenRol = (String) rolCombo.getSelectedItem();

            if (name.isEmpty() || tc.isEmpty() || password.isEmpty()) {
                statusRegLabel.setText("Lutfen tum alanlari doldurun!");
                statusRegLabel.setForeground(Color.RED);
                return;
            }
            if (tc.length() != 11) {
                statusRegLabel.setText("TC Kimlik No 11 haneli olmalidir!");
                statusRegLabel.setForeground(Color.RED);
                return;
            }

            statusRegLabel.setText("🔄 REST API uzerinden kayit yapiliyor...");
            statusRegLabel.setForeground(Color.BLUE);

            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    try {
                        com.akillikutup.core.Kullanici yeni;
                        if ("Admin".equals(secilenRol)) {
                            yeni = new com.akillikutup.core.Admin(name, tc, password);
                        } else {
                            yeni = new com.akillikutup.core.Uye(name, tc, password);
                        }
                        yeni.setEmail(email.isEmpty() ? name.replace(" ", ".").toLowerCase() + "@kutuphane.local" : email);
                        LibraryManager.getInstance().addUser(yeni);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(registerDialog,
                                secilenRol + " olarak REST API uzerinden kayit tamamlandi!",
                                "Basarili", JOptionPane.INFORMATION_MESSAGE);
                            registerDialog.dispose();
                        } else {
                            statusRegLabel.setText("❌ Kayit basarisiz! Isim/TC/Email cakisiyor olabilir.");
                            statusRegLabel.setForeground(Color.RED);
                        }
                    } catch (Exception e) {
                        statusRegLabel.setText("❌ Sunucu hatasi!");
                        statusRegLabel.setForeground(Color.RED);
                    }
                }
            };
            worker.execute();
        });

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        registerDialog.add(saveButton, gbc);
        registerDialog.setVisible(true);
    }
}
