package com.akillikutup.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthManagerTest {

    @Test
    public void testHashPassword() {
        AuthManager authManager = new AuthManager();
        String password = "test_password";
        byte[] salt = authManager.generateSalt();
        String hash = authManager.hashPassword(password, salt);

        assertNotNull(hash);
        assertNotEquals(password, hash);
    }
}
