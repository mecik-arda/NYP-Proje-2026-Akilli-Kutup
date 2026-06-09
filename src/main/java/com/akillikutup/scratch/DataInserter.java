package com.akillikutup.scratch;

import com.akillikutup.core.*;
import com.akillikutup.db.DatabaseManager;
import java.util.ArrayList;
import java.util.List;

public class DataInserter {
    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.tekOrnekAl();
        
        com.akillikutup.auth.AuthManager auth = new com.akillikutup.auth.AuthManager();
        List<Kullanici> users = new ArrayList<>();
        users.add(new Admin("Ahmet Güler", "11111111111", auth.registerPassword("12345678")));
        users.add(new Admin("Arda Meçik", "22222222222", auth.registerPassword("12345678")));
        users.add(new Admin("Eren Gider", "33333333333", auth.registerPassword("12345678")));
        users.add(new Uye("Göktuğ Berke Kuzucu", "44444444444", auth.registerPassword("12345678")));
        
        List<Materyal> materials = new ArrayList<>();
        materials.add(new Kitap("Yüzüklerin Efendisi", 5, 120.0, "978-0544003415"));
        materials.add(new Kitap("Suç ve Ceza", 3, 45.0, "978-0553211757"));
        materials.add(new Kitap("1984", 10, 55.0, "978-0451524935"));
        materials.add(new Kitap("Sefiller", 2, 85.0, "978-0451419439"));
        materials.add(new Kitap("Simyacı", 7, 40.0, "978-0061122415"));
        materials.add(new DijitalMedya("Java Programming Masterclass", 150.0, "MP4"));
        materials.add(new DijitalMedya("Advanced Python Guide", 90.0, "PDF"));
        materials.add(new DijitalMedya("AI in the Modern World", 200.0, "EPUB"));
        materials.add(new DijitalMedya("Data Structures & Algorithms", 110.0, "PDF"));
        materials.add(new Kitap("Clean Code", 4, 250.0, "978-0132350884"));
        materials.add(new Kitap("Design Patterns", 2, 200.0, "978-0201633610"));

        Materyal m1 = materials.get(0); 
        Materyal m2 = materials.get(1); 
        Materyal m3 = materials.get(5); 

        m1.oduncVer();
        users.get(2).materyalOduncAl(m1.getId()); 

        m2.oduncVer();
        m3.oduncVer();
        users.get(3).materyalOduncAl(m2.getId()); 
        users.get(3).materyalOduncAl(m3.getId()); 
        
        db.senkronizeEt(users, materials);
        
        System.out.println("Data successfully inserted!");
    }
}
