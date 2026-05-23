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
        users = new ArrayList<>();
        materials = new ArrayList<>();
        
        users.add(new Admin("Ahmet Guler (Admin)", "11122233344", "admin123"));
        users.add(new Admin("Eren Gider", "1231602061", "TestSifresi"));
        users.add(new Uye("Ayse Demir (Uye)", "55544433322", "uye123"));

        materials.add(new Kitap("1984", 5, 25.0, "978-0451524935"));
        materials.add(new Kitap("Seker Portakali", 3, 15.0, "978-9750719387"));
        materials.add(new DijitalMedya("Inception (Film)", 10.0, "MP4"));
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
}
