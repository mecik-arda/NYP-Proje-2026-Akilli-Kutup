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
        refreshUsers();
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
                List<Kullanici> users = LibraryManager.getInstance().getUsers();
                Kullanici selectedUser = users.get(selectedIndex);
                String enteredPassword = new String(passwordField.getPassword());
                com.akillikutup.auth.AuthManager auth = new com.akillikutup.auth.AuthManager();
                Kullanici loggedInUser = auth.login(selectedUser.getTcNoDogrudan(), enteredPassword);
                if (loggedInUser != null) {
                    LibraryManager.getInstance().setCurrentUser(loggedInUser);
                    passwordField.setText("");
                    if (loggedInUser.getRol().equals("ADMIN")) {
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

        JButton registerButton = new JButton("Kayit Ol");
        registerButton.addActionListener(e -> showRegisterDialog());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        add(registerButton, gbc);
    }

    private void showRegisterDialog() {
        JDialog registerDialog = new JDialog(mainFrame, "Yeni Kullanici Kaydi", true);
        registerDialog.setSize(400, 300);
        registerDialog.setLayout(new GridBagLayout());
        registerDialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel rolLabel = new JLabel("Rol:");
        gbc.gridx = 0; gbc.gridy = 0;
        registerDialog.add(rolLabel, gbc);

        JComboBox<String> rolComboBox = new JComboBox<>(new String[]{"Uye", "Admin"});
        gbc.gridx = 1; gbc.gridy = 0;
        registerDialog.add(rolComboBox, gbc);

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

        JLabel passLabel = new JLabel("Sifre:");
        gbc.gridx = 0; gbc.gridy = 3;
        registerDialog.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 3;
        registerDialog.add(passField, gbc);

        JButton saveButton = new JButton("Kaydet ve Kayit Ol");
        saveButton.addActionListener(event -> {
            String name = nameField.getText().trim();
            String tc = tcField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String secilenRol = (String) rolComboBox.getSelectedItem();

            if (name.isEmpty() || tc.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "Lutfen tum alanlari doldurun!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (tc.length() != 11) {
                JOptionPane.showMessageDialog(registerDialog, "TC Kimlik No 11 haneli olmalidir!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean nameExists = false;
            for (Kullanici u : LibraryManager.getInstance().getUsers()) {
                if (u.getIsim().equalsIgnoreCase(name)) {
                    nameExists = true;
                    break;
                }
            }
            if (nameExists) {
                JOptionPane.showMessageDialog(registerDialog, "Bu isimle kayitli bir kullanici zaten var!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String hashedPassword = new com.akillikutup.auth.AuthManager().registerPassword(password);
            Kullanici yeniKullanici;
            if ("Admin".equals(secilenRol)) {
                yeniKullanici = new com.akillikutup.core.Admin(name, tc, hashedPassword);
            } else {
                yeniKullanici = new com.akillikutup.core.Uye(name, tc, hashedPassword);
            }

            LibraryManager.getInstance().addUser(yeniKullanici);

            JOptionPane.showMessageDialog(registerDialog, secilenRol + " olarak kayit basariyla tamamlandi!", "Basarili", JOptionPane.INFORMATION_MESSAGE);

            refreshUsers();
            registerDialog.dispose();
        });

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        registerDialog.add(saveButton, gbc);

        registerDialog.setVisible(true);
    }

    public void refreshUsers() {
        userComboBox.removeAllItems();
        List<Kullanici> users = LibraryManager.getInstance().getUsers();
        for (Kullanici user : users) {
            userComboBox.addItem(user.getIsim());
        }
    }
}