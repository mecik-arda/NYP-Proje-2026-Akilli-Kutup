package com.akillikutup.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthManagerTest {

    @Test
    public void testHashPassword() {
        AuthManager authManager = new AuthManager();
        String password = "test_password";
        String hash = authManager.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertEquals(64, hash.length());
    }
}
