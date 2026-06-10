package io.github.veereshhanni.mcrypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * mcrypt Production Test - Java (Maven)
 * ========================================
 * Install: Add mcrypt-mass-crypto-java to pom.xml
 * Run:     mvn test
 *
 * This test simulates how end-users import and use mcrypt
 * after adding the Maven dependency:
 *   import io.github.veereshhanni.mcrypt.Mcrypt;
 */
class McryptProductionTest {

    // ---------- Scenario: User Registration ----------

    @Test
    @DisplayName("User Registration - hash password for DB storage")
    void userRegistration() {
        String password = "MySecureP@ssw0rd!2026";

        // Step 1: Generate salt
        String salt = Mcrypt.gensalt(12);
        assertNotNull(salt);
        assertTrue(salt.startsWith("$mcrypt$v3$"));

        // Step 2: Hash password
        String storedHash = Mcrypt.hashWithSalt(password, salt);
        assertNotNull(storedHash);
        assertTrue(storedHash.length() > salt.length());
    }

    // ---------- Scenario: User Login ----------

    @Test
    @DisplayName("User Login - correct password succeeds")
    void loginCorrectPassword() {
        String password = "MySecureP@ssw0rd!2026";
        String salt = Mcrypt.gensalt(12);
        String storedHash = Mcrypt.hashWithSalt(password, salt);

        assertTrue(Mcrypt.verify(password, storedHash),
                "Login with correct password should succeed");
    }

    @Test
    @DisplayName("User Login - wrong password fails")
    void loginWrongPassword() {
        String password = "MySecureP@ssw0rd!2026";
        String salt = Mcrypt.gensalt(12);
        String storedHash = Mcrypt.hashWithSalt(password, salt);

        assertFalse(Mcrypt.verify("hacker123", storedHash),
                "Login with wrong password should fail");
    }

    @Test
    @DisplayName("User Login - similar password fails")
    void loginSimilarPassword() {
        String password = "MySecureP@ssw0rd!2026";
        String salt = Mcrypt.gensalt(12);
        String storedHash = Mcrypt.hashWithSalt(password, salt);

        assertFalse(Mcrypt.verify("MySecureP@ssw0rd!2027", storedHash),
                "Login with similar password should fail");
    }

    @Test
    @DisplayName("User Login - empty password fails")
    void loginEmptyPassword() {
        String salt = Mcrypt.gensalt(12);
        String storedHash = Mcrypt.hashWithSalt("RealPassword", salt);

        assertFalse(Mcrypt.verify("", storedHash),
                "Login with empty password should fail");
    }

    // ---------- Scenario: Password Change ----------

    @Test
    @DisplayName("Password Change - old password invalid after change")
    void passwordChangeOldInvalid() {
        String oldPassword = "OldP@ss123";
        String newPassword = "NewStr0ngerP@ss!2026";

        String oldSalt = Mcrypt.gensalt(12);
        String oldHash = Mcrypt.hashWithSalt(oldPassword, oldSalt);

        String newSalt = Mcrypt.gensalt(12);
        String newHash = Mcrypt.hashWithSalt(newPassword, newSalt);

        // Old password should NOT work against new hash
        assertFalse(Mcrypt.verify(oldPassword, newHash));

        // New password should work against new hash
        assertTrue(Mcrypt.verify(newPassword, newHash));

        // Old hash should still work with old password (until DB update)
        assertTrue(Mcrypt.verify(oldPassword, oldHash));
    }

    // ---------- Scenario: Batch Hashing (Mass Crypto) ----------

    @Test
    @DisplayName("Batch Hashing - 5 users hash and verify correctly")
    void batchHashing() {
        String[][] users = {
                {"alice", "Alice@Pass123"},
                {"bob", "B0bSecure!"},
                {"charlie", "Ch@rlie2026"},
                {"diana", "D1ana_Strong"},
                {"eve", "Ev3_P@ssword"},
        };

        String[] hashes = new String[users.length];

        // Hash all passwords
        for (int i = 0; i < users.length; i++) {
            String salt = Mcrypt.gensalt(10);
            hashes[i] = Mcrypt.hashWithSalt(users[i][1], salt);
        }

        // Verify all users
        for (int i = 0; i < users.length; i++) {
            assertTrue(Mcrypt.verify(users[i][1], hashes[i]),
                    users[i][0] + "'s password should verify correctly");
        }

        // Cross-verify: alice's password should NOT match bob's hash
        assertFalse(Mcrypt.verify(users[0][1], hashes[1]),
                "Alice's password should NOT match Bob's hash");
    }

    // ---------- Scenario: Complete E2E Authentication Flow ----------

    @Test
    @DisplayName("E2E - Sign up, login, fail, change password, login again")
    void completeE2EFlow() {
        // 1. Sign up
        String signupPassword = "KoppalGadag@2026";
        String salt = Mcrypt.gensalt(12);
        String dbHash = Mcrypt.hashWithSalt(signupPassword, salt);

        // 2. Login (success)
        assertTrue(Mcrypt.verify(signupPassword, dbHash));

        // 3. Failed login attempt
        assertFalse(Mcrypt.verify("wrongPassword", dbHash));

        // 4. Change password
        String newPassword = "NewGadag@2027";
        String newSalt = Mcrypt.gensalt(12);
        String newDbHash = Mcrypt.hashWithSalt(newPassword, newSalt);

        // 5. Login with new password (success)
        assertTrue(Mcrypt.verify(newPassword, newDbHash));

        // 6. Old password no longer works
        assertFalse(Mcrypt.verify(signupPassword, newDbHash));
    }
}
