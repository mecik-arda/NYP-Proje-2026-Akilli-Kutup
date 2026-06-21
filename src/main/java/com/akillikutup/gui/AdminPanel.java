package com.akillikutup.gui;

import com.akillikutup.core.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {
    private MainFrame mainFrame;
    private JLabel welcomeLabel;
    private JLabel statsLabel;

    private JTable materialTable;
    private DefaultTableModel materialTableModel;

    private JTable userTable;
    private DefaultTableModel userTableModel;

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel leftTop = new JPanel(new GridLayout(2, 1));
        welcomeLabel = new JLabel("Admin Paneli");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statsLabel = new JLabel("İstatistikler yükleniyor...");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        leftTop.add(welcomeLabel);
        leftTop.add(statsLabel);
        topPanel.add(leftTop, BorderLayout.WEST);

        JButton logoutButton = new JButton("Çıkış Yap (JWT temizle)");
        logoutButton.addActionListener(e -> {
            LibraryManager.getInstance().logout();
            mainFrame.showPanel("LOGIN");
        });
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel materyalPanel = new JPanel(new BorderLayout());

        String[] matColumnNames = {"ID", "Başlık", "Tür", "Stok/Erişim", "Fiyat"};
        materialTableModel = new DefaultTableModel(matColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        materialTable = new JTable(materialTableModel);
        JScrollPane matScrollPane = new JScrollPane(materialTable);
        materyalPanel.add(matScrollPane, BorderLayout.CENTER);

        JPanel matBottomPanel = new JPanel(new BorderLayout());

        JPanel matAddPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        matAddPanel.setBorder(BorderFactory.createTitledBorder("Yeni Materyal Ekle"));

        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Kitap", "Dijital Medya"});
        JTextField baslikField = new JTextField(10);
        JTextField fiyatField = new JTextField(5);
        JTextField extraField = new JTextField(10);
        JTextField stokField = new JTextField(5);

        matAddPanel.add(new JLabel("Tür:"));
        matAddPanel.add(typeCombo);
        matAddPanel.add(new JLabel("Başlık:"));
        matAddPanel.add(baslikField);
        matAddPanel.add(new JLabel("Birim Fiyat:"));
        matAddPanel.add(fiyatField);
        JLabel extraLabel = new JLabel("ISBN:");
        matAddPanel.add(extraLabel);
        matAddPanel.add(extraField);
        JLabel stokLabel = new JLabel("Stok:");
        matAddPanel.add(stokLabel);
        matAddPanel.add(stokField);

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
            matAddPanel.revalidate();
            matAddPanel.repaint();
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
                    yeniMateryal = new DijitalMedya(baslik, fiyat, extra, "E-Kitap", "0 MB");
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
                JOptionPane.showMessageDialog(this, "Materyal başarıyla eklendi.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Hata: Girdileri kontrol edin.", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        });
        matAddPanel.add(addButton);
        matBottomPanel.add(matAddPanel, BorderLayout.CENTER);

        JPanel matDeletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteMaterialButton = new JButton("Seçili Materyali Sil");
        deleteMaterialButton.addActionListener(e -> {
            int selectedRow = materialTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Lütfen silmek için bir materyal seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String id = (String) materialTableModel.getValueAt(selectedRow, 0);
            String baslik = (String) materialTableModel.getValueAt(selectedRow, 1);

            int onay = JOptionPane.showConfirmDialog(this,
                    "\"" + baslik + "\" adlı materyali silmek istediğinize emin misiniz?",
                    "Silme Onayı", JOptionPane.YES_NO_OPTION);
            if (onay == JOptionPane.YES_OPTION) {
                Materyal silinecek = null;
                for (Materyal m : LibraryManager.getInstance().getMaterials()) {
                    if (m.getId().equals(id)) {
                        silinecek = m;
                        break;
                    }
                }
                if (silinecek != null) {
                    LibraryManager.getInstance().removeMaterial(silinecek);
                    refreshData();
                    JOptionPane.showMessageDialog(this, "Materyal REST API uzerinden silindi.");
                }
            }
        });
        matDeletePanel.add(deleteMaterialButton);
        matBottomPanel.add(matDeletePanel, BorderLayout.SOUTH);

        materyalPanel.add(matBottomPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Materyal Yönetimi", materyalPanel);

        JPanel uyePanel = new JPanel(new BorderLayout());

        String[] userColumnNames = {"Isim", "Rol", "TC Kimlik No", "E-posta", "Kredi Puani"};
        userTableModel = new DefaultTableModel(userColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        userTable = new JTable(userTableModel);
        JScrollPane userScrollPane = new JScrollPane(userTable);
        uyePanel.add(userScrollPane, BorderLayout.CENTER);

        JPanel userButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userButtonPanel.setBorder(BorderFactory.createTitledBorder("Üye İşlemleri"));

        JButton detailButton = new JButton("Detay Görüntüle");
        detailButton.addActionListener(e -> {
            Kullanici secilen = getSelectedUser();
            if (secilen == null) return;
            Kullanici admin = LibraryManager.getInstance().getCurrentUser();
            String tcBilgi = secilen.getTcNo(admin);
            String detay = "=== Kullanıcı Detayları ===\n"
                    + "İsim: " + secilen.getIsim() + "\n"
                    + "Rol: " + secilen.getRol() + "\n"
                    + "TC Kimlik No: " + tcBilgi + "\n"
                    + "Kredi Puanı: " + secilen.getKrediPuani() + "\n"
                    + "Şifre: " + secilen.getSifre();
            JOptionPane.showMessageDialog(this, detay, "Kullanıcı Detay", JOptionPane.INFORMATION_MESSAGE);
        });
        userButtonPanel.add(detailButton);

        JButton deleteUserButton = new JButton("Üyeyi Sil");
        deleteUserButton.addActionListener(e -> {
            Kullanici secilen = getSelectedUser();
            if (secilen == null) return;

            Kullanici currentAdmin = LibraryManager.getInstance().getCurrentUser();
            if (currentAdmin != null && currentAdmin.getIsim().equals(secilen.getIsim())) {
                JOptionPane.showMessageDialog(this, "Kendinizi silemezsiniz!", "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int onay = JOptionPane.showConfirmDialog(this,
                    "\"" + secilen.getIsim() + "\" adlı kullanıcıyı silmek istediğinize emin misiniz?",
                    "Silme Onayı", JOptionPane.YES_NO_OPTION);
            if (onay == JOptionPane.YES_OPTION) {
                LibraryManager.getInstance().removeUser(secilen);
                refreshData();
                JOptionPane.showMessageDialog(this, "Kullanici REST API uzerinden silindi.");
            }
        });
        userButtonPanel.add(deleteUserButton);

        JButton updatePointsButton = new JButton("Kredi Güncelle");
        updatePointsButton.addActionListener(e -> {
            Kullanici secilen = getSelectedUser();
            if (secilen == null) return;
            if (secilen instanceof Admin) {
                JOptionPane.showMessageDialog(this, "Admin kullanıcıların kredi puanı değiştirilemez.", "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String input = JOptionPane.showInputDialog(this,
                    secilen.getIsim() + " için yeni kredi puanı (mevcut: " + secilen.getKrediPuani() + "):",
                    "Kredi Puanı Güncelle", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int yeniPuan = Integer.parseInt(input.trim());
                    int fark = yeniPuan - secilen.getKrediPuani();
                    ((Uye) secilen).puanGuncelle(fark);
                    refreshData();
                    JOptionPane.showMessageDialog(this, "Kredi puanı güncellendi: " + secilen.getKrediPuani());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Geçersiz sayı girdiniz.", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        userButtonPanel.add(updatePointsButton);

        JButton changeTcButton = new JButton("TC Değiştir");
        changeTcButton.addActionListener(e -> {
            Kullanici secilen = getSelectedUser();
            if (secilen == null) return;
            Kullanici admin = LibraryManager.getInstance().getCurrentUser();
            String mevcutTc = secilen.getTcNo(admin);
            String yeniTc = JOptionPane.showInputDialog(this,
                    secilen.getIsim() + " için yeni TC Kimlik No (mevcut: " + mevcutTc + "):",
                    "TC Kimlik No Değiştir", JOptionPane.PLAIN_MESSAGE);
            if (yeniTc != null && !yeniTc.trim().isEmpty()) {
                if (yeniTc.trim().length() != 11) {
                    JOptionPane.showMessageDialog(this, "TC Kimlik No 11 haneli olmalıdır!", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                secilen.setTcNo(yeniTc.trim());
                LibraryManager.getInstance().updateUser(secilen.getId(), secilen);
                refreshData();
                JOptionPane.showMessageDialog(this, "TC Kimlik No REST API uzerinden guncellendi.");
            }
        });
        userButtonPanel.add(changeTcButton);

        JButton resetPassButton = new JButton("Şifre Sıfırla");
        resetPassButton.addActionListener(e -> {
            Kullanici secilen = getSelectedUser();
            if (secilen == null) return;
            String yeniSifre = JOptionPane.showInputDialog(this,
                    secilen.getIsim() + " için yeni şifre girin:",
                    "Şifre Sıfırla", JOptionPane.PLAIN_MESSAGE);
            if (yeniSifre != null && !yeniSifre.trim().isEmpty()) {
                secilen.setSifre(yeniSifre.trim());
                LibraryManager.getInstance().updateUser(secilen.getId(), secilen);
                refreshData();
                JOptionPane.showMessageDialog(this, "Sifre REST API uzerinden guncellendi.");
            }
        });
        userButtonPanel.add(resetPassButton);

        JButton changeNameButton = new JButton("İsim Değiştir");
        changeNameButton.addActionListener(e -> {
            Kullanici secilen = getSelectedUser();
            if (secilen == null) return;
            String yeniIsim = JOptionPane.showInputDialog(this,
                    "Mevcut isim: " + secilen.getIsim() + "\nYeni isim girin:",
                    "İsim Değiştir", JOptionPane.PLAIN_MESSAGE);
            if (yeniIsim != null && !yeniIsim.trim().isEmpty()) {
                boolean exists = false;
                for (Kullanici k : LibraryManager.getInstance().getUsers()) {
                    if (k.getIsim().equalsIgnoreCase(yeniIsim.trim()) && k != secilen) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    JOptionPane.showMessageDialog(this, "Bu isimde bir kullanıcı zaten var!", "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                secilen.setIsim(yeniIsim.trim());
                LibraryManager.getInstance().updateUser(secilen.getId(), secilen);
                refreshData();
                JOptionPane.showMessageDialog(this, "Isim REST API uzerinden guncellendi.");
            }
        });
        userButtonPanel.add(changeNameButton);

        uyePanel.add(userButtonPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Üye Yönetimi", uyePanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private Kullanici getSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Lütfen bir kullanıcı seçin.", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String isim = (String) userTableModel.getValueAt(selectedRow, 0);
        for (Kullanici k : LibraryManager.getInstance().getUsers()) {
            if (k.getIsim().equals(isim)) {
                return k;
            }
        }
        return null;
    }

    public void refreshData() {
        Kullanici currentUser = LibraryManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Hoş geldiniz, " + currentUser.getIsim() + " (Admin)");
        }

        int toplamUye = 0, toplamAdmin = 0;
        for (Kullanici k : LibraryManager.getInstance().getUsers()) {
            if (k.getRol().equals("ADMIN")) toplamAdmin++;
            else toplamUye++;
        }
        int toplamMateryal = LibraryManager.getInstance().getMaterials().size();
        statsLabel.setText("Toplam: " + toplamAdmin + " Admin, " + toplamUye + " Üye, " + toplamMateryal + " Materyal");

        materialTableModel.setRowCount(0);
        List<Materyal> materials = LibraryManager.getInstance().getMaterials();
        for (Materyal m : materials) {
            String tur = m instanceof Kitap ? "Kitap" : "Dijital Medya";
            String stok = m instanceof Kitap ? String.valueOf(m.getStokAdedi()) : "Sınırsız";
            materialTableModel.addRow(new Object[]{m.getId(), m.getBaslik(), tur, stok, m.getBirimFiyat()});
        }

        userTableModel.setRowCount(0);
        Kullanici admin = LibraryManager.getInstance().getCurrentUser();
        List<Kullanici> users = LibraryManager.getInstance().getUsers();
        for (Kullanici k : users) {
            String tcBilgi = (admin != null) ? k.getTcNo(admin) : k.getTcNoDogrudan();
            String email = k.getEmail() != null ? k.getEmail() : "Yok";
            userTableModel.addRow(new Object[]{k.getIsim(), k.getRol(), tcBilgi, email, k.getKrediPuani()});
        }
        statsLabel.setText(statsLabel.getText() + " [REST API ✅]");
    }
}