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
    private JLabel rolLabel;
    private JTable materialTable;
    private DefaultTableModel tableModel;

    public UserPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        // --- UST PANEL ---
        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));

        welcomeLabel = new JLabel("Uye Paneli");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel detailLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolLabel = new JLabel("Rol: UYE");
        rolLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        pointsLabel = new JLabel("Kredi Puani: 100");
        pointsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        pointsLabel.setForeground(new Color(0, 128, 0));

        detailLine.add(rolLabel);
        detailLine.add(new JLabel("  |  "));
        detailLine.add(pointsLabel);

        userInfoPanel.add(welcomeLabel);
        userInfoPanel.add(Box.createVerticalStrut(4));
        userInfoPanel.add(detailLine);

        topPanel.add(userInfoPanel, BorderLayout.WEST);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton profileButton = new JButton("Profilim");
        profileButton.addActionListener(e -> {
            Kullanici currentUser = LibraryManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                String profil = "=== Profil Bilgileri ===\n"
                        + "Isim: " + currentUser.getIsim() + "\n"
                        + "Rol: " + currentUser.getRol() + "\n"
                        + "Kredi Puani: " + currentUser.getKrediPuani() + "\n"
                        + "TC Kimlik No: " + currentUser.getTcNo(currentUser);
                JOptionPane.showMessageDialog(this, profil, "Profilim", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        rightButtons.add(profileButton);

        JButton logoutButton = new JButton("Cikis Yap");
        logoutButton.addActionListener(e -> {
            LibraryManager.getInstance().setCurrentUser(null);
            mainFrame.showPanel("LOGIN");
        });
        rightButtons.add(logoutButton);

        topPanel.add(rightButtons, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- MATERYAL TABLOSU ---
        String[] columnNames = {"ID", "Baslik", "Tur", "Stok/Erisim", "Fiyat"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        materialTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(materialTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- ALT PANEL ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Arama
        bottomPanel.add(new JLabel("Ara:"));
        JTextField searchField = new JTextField(12);
        bottomPanel.add(searchField);

        JButton searchButton = new JButton("Ara");
        searchButton.addActionListener(e -> {
            String arama = searchField.getText().trim().toLowerCase();
            tableModel.setRowCount(0);
            List<Materyal> materials = LibraryManager.getInstance().getMaterials();
            for (Materyal m : materials) {
                if (arama.isEmpty() || m.getBaslik().toLowerCase().contains(arama)) {
                    String tur = m instanceof Kitap ? "Kitap" : "Dijital Medya";
                    String stok = m instanceof Kitap ? String.valueOf(m.getStokAdedi()) : "Sinirsiz";
                    tableModel.addRow(new Object[]{m.getId(), m.getBaslik(), tur, stok, m.getBirimFiyat()});
                }
            }
        });
        bottomPanel.add(searchButton);

        JButton showAllButton = new JButton("Tumunu Goster");
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            refreshData();
        });
        bottomPanel.add(showAllButton);

        bottomPanel.add(Box.createHorizontalStrut(20));

        // Odunc Al
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
                        if (uye.getKrediPuani() < 20) {
                            JOptionPane.showMessageDialog(this,
                                    "Kredi puaniniz cok dusuk (" + uye.getKrediPuani() + ")! Odunc alamazsiniz.",
                                    "Yetersiz Kredi", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        uye.materyalAl(selectedMaterial);
                        uye.puanGuncelle(-5);
                        JOptionPane.showMessageDialog(this,
                                "\"" + selectedMaterial.getBaslik() + "\" basariyla odunc alindi!\nKalan Kredi: " + uye.getKrediPuani());
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
            rolLabel.setText("Rol: " + currentUser.getRol());

            int puan = currentUser.getKrediPuani();
            pointsLabel.setText("Kredi Puani: " + puan);
            if (puan >= 60) {
                pointsLabel.setForeground(new Color(0, 128, 0));
            } else if (puan >= 30) {
                pointsLabel.setForeground(new Color(200, 150, 0));
            } else {
                pointsLabel.setForeground(Color.RED);
            }
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