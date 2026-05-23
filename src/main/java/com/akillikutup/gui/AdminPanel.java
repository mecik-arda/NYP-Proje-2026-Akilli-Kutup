package com.akillikutup.gui;

import com.akillikutup.core.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel welcomeLabel;
    private JTable materialTable;
    private DefaultTableModel tableModel;

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Admin Paneli");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Cikis Yap");
        logoutButton.addActionListener(e -> {
            LibraryManager.getInstance().setCurrentUser(null);
            mainFrame.showPanel("LOGIN");
        });
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Baslik", "Tur", "Stok/Erisim", "Fiyat"};
        tableModel = new DefaultTableModel(columnNames, 0);
        materialTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(materialTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Yeni Materyal Ekle"));

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Kitap", "Dijital Medya"});
        JTextField baslikField = new JTextField(10);
        JTextField fiyatField = new JTextField(5);
        JTextField extraField = new JTextField(10);
        JTextField stokField = new JTextField(5);

        bottomPanel.add(new JLabel("Tur:"));
        bottomPanel.add(typeCombo);
        bottomPanel.add(new JLabel("Baslik:"));
        bottomPanel.add(baslikField);
        bottomPanel.add(new JLabel("Birim Fiyat:"));
        bottomPanel.add(fiyatField);
        JLabel extraLabel = new JLabel("ISBN:");
        bottomPanel.add(extraLabel);
        bottomPanel.add(extraField);
        JLabel stokLabel = new JLabel("Stok:");
        bottomPanel.add(stokLabel);
        bottomPanel.add(stokField);

        typeCombo.addActionListener(e -> {
            if (typeCombo.getSelectedIndex() == 0) {
                extraLabel.setText("ISBN:");
                stokField.setVisible(true);
                stokLabel.setVisible(true);
            } else {
                extraLabel.setText("Format:");
                stokField.setVisible(false);
                stokLabel.setVisible(false);
            }
            bottomPanel.revalidate();
            bottomPanel.repaint();
        });

        JButton addButton = new JButton("Ekle");
        addButton.addActionListener(e -> {
            try {
                String baslik = baslikField.getText();
                double fiyat = Double.parseDouble(fiyatField.getText());
                String extra = extraField.getText();

                Materyal yeniMateryal;
                if (typeCombo.getSelectedIndex() == 0) {
                    int stok = Integer.parseInt(stokField.getText());
                    yeniMateryal = new Kitap(baslik, stok, fiyat, extra);
                } else {
                    yeniMateryal = new DijitalMedya(baslik, fiyat, extra);
                }

                Kullanici user = LibraryManager.getInstance().getCurrentUser();
                if (user instanceof Admin) {
                    ((Admin) user).envanterEkle(yeniMateryal);
                }
                LibraryManager.getInstance().addMaterial(yeniMateryal);
                refreshData();
                
                baslikField.setText("");
                fiyatField.setText("");
                extraField.setText("");
                stokField.setText("");
                JOptionPane.showMessageDialog(this, "Materyal basariyla eklendi.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Hata: Girdileri kontrol edin.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomPanel.add(addButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        Kullanici currentUser = LibraryManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Hosgeldin, " + currentUser.getIsim() + " (Admin)");
        }

        tableModel.setRowCount(0);
        List<Materyal> materials = LibraryManager.getInstance().getMaterials();
        for (Materyal m : materials) {
            String tur = m instanceof Kitap ? "Kitap" : "Dijital Medya";
            String stok = m instanceof Kitap ? String.valueOf(m.getStokAdedi()) : "Sinirsiz";
            tableModel.addRow(new Object[]{m.getId(), m.getBaslik(), tur, stok, m.getBirimFiyat()});
        }
    }
}
