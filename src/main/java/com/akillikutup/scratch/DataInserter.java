package com.akillikutup.scratch;

import com.akillikutup.material.*;
import com.akillikutup.user.User;
import com.akillikutup.db.DatabaseManager;
import java.util.ArrayList;
import java.util.List;

public class DataInserter {
    public static void main(String[] args) {
        DatabaseManager db = DatabaseManager.tekOrnekAl();

        com.akillikutup.auth.AuthManager auth = new com.akillikutup.auth.AuthManager();
        List<User> users = new ArrayList<>();
        users.add(new User("Ahmet Güler", "11111111111", User.Role.ADMIN, auth.registerPassword("12345678")));
        users.add(new User("Arda Meçik", "22222222222", User.Role.ADMIN, auth.registerPassword("12345678")));
        users.add(new User("Eren Gider", "33333333333", User.Role.ADMIN, auth.registerPassword("12345678")));
        users.add(new User("Göktuğ Berke Kuzucu", "44444444444", User.Role.UYE, auth.registerPassword("12345678")));

        List<Materyal> materials = new ArrayList<>();
        materials.add(new Kitap("Yüzüklerin Efendisi", 5, 120.0, "978-0544003415"));
        materials.add(new Kitap("Suç ve Ceza", 3, 45.0, "978-0553211757"));
        materials.add(new Kitap("1984", 10, 55.0, "978-0451524935"));
        materials.add(new Kitap("Refactoring", 6, 85.0, "9780201485677"));
        materials.add(new Kitap("Design Patterns", 4, 120.0, "9780201633610"));
        materials.add(new DijitalMedya("Java Programming Masterclass", 150.0, "MP4", "Video", "2 GB"));
        materials.add(new DijitalMedya("Advanced Python Guide", 90.0, "PDF", "E-Kitap", "10 MB"));
        materials.add(new DijitalMedya("AI in the Modern World", 200.0, "EPUB", "E-Kitap", "15 MB"));
        materials.add(new DijitalMedya("Data Structures & Algorithms", 110.0, "PDF", "E-Kitap", "20 MB"));

        Materyal m1 = materials.get(0);
        Materyal m2 = materials.get(1);
        Materyal m3 = materials.get(5);

        if (m1 instanceof IOduncAlinabilir) ((IOduncAlinabilir) m1).oduncVer();
        users.get(2).materyalOduncAl(m1.getId());

        if (m2 instanceof IOduncAlinabilir) ((IOduncAlinabilir) m2).oduncVer();
        if (m3 instanceof IOduncAlinabilir) ((IOduncAlinabilir) m3).oduncVer();
        users.get(3).materyalOduncAl(m2.getId());
        users.get(3).materyalOduncAl(m3.getId());

        db.senkronizeEt(users, materials);

        System.out.println("Data successfully inserted!");
    }
}
