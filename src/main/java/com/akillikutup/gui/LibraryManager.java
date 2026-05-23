package com.akillikutup.gui;

import com.akillikutup.core.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryManager {
    private static LibraryManager instance;
    private List<Kullanici> users;
    private List<Materyal> materials;
    private Kullanici currentUser;

    private LibraryManager() {
        users = new java.util.ArrayList<>();
        materials = new java.util.ArrayList<>();

        users = com.akillikutup.db.DatabaseManager.tekOrnekAl().kullanicilariYukle();
        materials = com.akillikutup.db.DatabaseManager.tekOrnekAl().materyallariYukle();

        if (users.isEmpty()) {
            users.add(new Admin("Ahmet Guler", "11122233344", "admin123"));
            users.add(new Admin("Eren Gider", "1231602061", "TestSifresi"));
            users.add(new Uye("Ayse Demir", "55544433322", "uye123"));
            users.add(new Uye("Mehmet Yilmaz", "12345678901", "mehmet1"));
            users.add(new Uye("Fatma Kaya", "98765432100", "fatma1"));
            users.add(new Uye("Ali Celik", "11223344556", "ali1"));
            users.add(new Uye("Zeynep Ozturk", "66778899001", "zeynep1"));
            com.akillikutup.db.DatabaseManager.tekOrnekAl().kaydet(users, materials);
        }

        if (materials.isEmpty()) {
            materials.add(new Kitap("1984", 5, 25.0, "978-0451524935"));
            materials.add(new Kitap("Seker Portakali", 3, 15.0, "978-9750719387"));
            materials.add(new Kitap("Kucuk Prens", 7, 20.0, "978-9750726439"));
            materials.add(new Kitap("Suç ve Ceza", 4, 30.0, "978-9754580662"));
            materials.add(new Kitap("Simyaci", 6, 22.0, "978-9750726446"));
            materials.add(new DijitalMedya("Inception (Film)", 10.0, "MP4"));
            materials.add(new DijitalMedya("Interstellar (Film)", 12.0, "MP4"));
            materials.add(new DijitalMedya("Java Dersleri (Video)", 0.0, "MP4"));
            com.akillikutup.db.DatabaseManager.tekOrnekAl().kaydet(users, materials);
        }
    }

    public void addUser(Kullanici user) {
        this.users.add(user);
        com.akillikutup.db.DatabaseManager.tekOrnekAl().kullaniciEkle(user);
    }

    public static LibraryManager getInstance() {
        if (instance == null) {
            instance = new LibraryManager();
        }
        return instance;
    }

    public List<Kullanici> getUsers() {
        return users;
    }

    public List<Materyal> getMaterials() {
        return materials;
    }

    public Kullanici getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Kullanici currentUser) {
        this.currentUser = currentUser;
    }

    public void addMaterial(Materyal m) {
        materials.add(m);
    }

    public void removeUser(Kullanici user) {
        users.remove(user);
    }

    public void removeMaterial(Materyal m) {
        materials.remove(m);
    }
}