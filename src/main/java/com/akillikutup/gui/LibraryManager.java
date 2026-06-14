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
            com.akillikutup.auth.AuthManager auth = new com.akillikutup.auth.AuthManager();
            users.add(new Admin("Ahmet Guler", "11111111111", auth.registerPassword("12345678")));
            users.add(new Admin("Arda Mecik", "22222222222", auth.registerPassword("12345678")));
            users.add(new Admin("Eren Gider", "33333333333", auth.registerPassword("12345678")));
            users.add(new Uye("Goktug Berke Kuzucu", "44444444444", auth.registerPassword("12345678")));
            com.akillikutup.db.DatabaseManager.tekOrnekAl().kaydet(users, materials);
        }

        if (materials.isEmpty()) {
            materials.add(new Kitap("1984", 5, 25.0, "978-0451524935"));
            materials.add(new Kitap("Seker Portakali", 3, 15.0, "978-9750719387"));
            materials.add(new Kitap("Kucuk Prens", 7, 20.0, "978-9750726439"));
            materials.add(new Kitap("Clean Code", 5, 50.0, "9780132350884"));
            materials.add(new Kitap("Effective Java", 3, 60.0, "9780134685991"));
            materials.add(new DijitalMedya("Inception (Film)", 10.0, "MP4", "Video", "1 GB"));
            materials.add(new DijitalMedya("Interstellar (Film)", 12.0, "MP4", "Video", "1.5 GB"));
            materials.add(new DijitalMedya("Java Dersleri (Video)", 0.0, "MP4", "Video", "500 MB"));
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
        return com.akillikutup.db.DatabaseManager.tekOrnekAl().getKullaniciListesi();
    }

    public List<Materyal> getMaterials() {
        return com.akillikutup.db.DatabaseManager.tekOrnekAl().getMateryalListesi();
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