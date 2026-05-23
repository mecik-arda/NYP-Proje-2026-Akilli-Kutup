package com.akillikutup.gui;

import com.akillikutup.core.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel welcomeLabel;
    private JLabel pointsLabel;
    private JTable materialTable;
    private DefaultTableModel tableModel;

    public UserPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomeLabel = new JLabel("Uye Paneli");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        pointsLabel = new JLabel(" | Kredi: ");
        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(pointsLabel);
        topPanel.add(userInfoPanel, BorderLayout.WEST);

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
        JButton borrowButton = new JButton("Secili Materyali Odunc Al");
        borrowButton.addActionListener(e -> {
            int selectedRow = materialTable.getSelectedRow();
            if (selectedRow >= 0) {
                String id = (String) tableModel.getValueAt(selectedRow, 0);
                Materyal selectedMaterial = null;
                for (Materyal m : LibraryManager.getInstance().getMaterials()) {
                    if (m.getId().equals(id)) {
                        selectedMaterial = m;
                        break;
                    }
                }

                if (selectedMaterial != null) {
                    Kullanici currentUser = LibraryManager.getInstance().getCurrentUser();
                    if (currentUser instanceof Uye) {
                        Uye uye = (Uye) currentUser;
                        uye.materyalAl(selectedMaterial);
                        uye.puanGuncelle(-5);
                        JOptionPane.showMessageDialog(this, "Islem gerceklestirildi. Konsolu kontrol edin.\nYeni Kredi: " + uye.getKrediPuani());
                        refreshData();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lutfen odunc almak icin bir materyal secin.");
            }
        });
        bottomPanel.add(borrowButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        Kullanici currentUser = LibraryManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Hosgeldin, " + currentUser.getIsim());
            pointsLabel.setText(" | Kredi: " + currentUser.getKrediPuani());
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
