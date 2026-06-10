package io.github.veereshhanni.mcrypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * mcrypt (Mass Crypto) - Java Binding Test Suite
 * ================================================
 * Run: cd bindings/java && mvn test
 */
class McryptTest {

    // ────────── gensalt() Tests ──────────

    @Test
    @DisplayName("gensalt returns valid mcrypt format")
    void gensaltReturnsValidFormat() {
        String salt = Mcrypt.gensalt(12);

        assertNotNull(salt, "Salt should not be null");
        assertTrue(salt.startsWith("$mcrypt$"), "Salt should start with $mcrypt$");
        assertTrue(salt.contains("$v3$"), "Salt should contain version $v3$");
        assertTrue(salt.contains("$r12$"), "Salt should contain round info $r12$");
        assertTrue(salt.contains("$sl16$"), "Salt should contain salt length $sl16$");
    }

    @Test
    @DisplayName("gensalt with different rounds")
    void gensaltDifferentRounds() {
        String salt4 = Mcrypt.gensalt(4);
        assertTrue(salt4.contains("$r04$"), "rounds=4 should have $r04$");

        String salt20 = Mcrypt.gensalt(20);
        assertTrue(salt20.contains("$r20$"), "rounds=20 should have $r20$");
    }

    @Test
    @DisplayName("gensalt produces unique salts")
    void gensaltProducesUniqueSalts() {
        String saltA = Mcrypt.gensalt(12);
        String saltB = Mcrypt.gensalt(12);
        assertNotEquals(saltA, saltB, "Two gensalt calls should produce different salts");
    }

    @Test
    @DisplayName("gensalt with custom salt length")
    void gensaltCustomSaltLength() {
        String salt = Mcrypt.gensalt(12, 32);
        assertNotNull(salt, "Salt with custom length should not be null");
        assertTrue(salt.startsWith("$mcrypt$"), "Salt should start with $mcrypt$");
    }

    // ────────── hashWithSalt() Tests ──────────

    @Test
    @DisplayName("hashWithSalt returns valid hash format")
    void hashWithSaltReturnsValidFormat() {
        String salt = Mcrypt.gensalt(12);
        String hash = Mcrypt.hashWithSalt("KoppalGadag@2026", salt);

        assertNotNull(hash, "Hash should not be null");
        assertTrue(hash.startsWith(salt), "Hash should start with salt prefix");
        assertTrue(hash.length() > salt.length(), "Hash should be longer than salt");

        // Format: $mcrypt$v3$r12$sl16$<salt>$<64-char-hex>
        String[] parts = hash.split("\\$");
        assertEquals(7, parts.length, "Hash should have 7 parts when split by $");
        assertEquals(64, parts[6].length(), "Hash digest should be 64 chars (SHA-256 hex)");
    }

    @Test
    @DisplayName("hashWithSalt is deterministic")
    void hashWithSaltIsDeterministic() {
        String salt = Mcrypt.gensalt(12);
        String hash1 = Mcrypt.hashWithSalt("TestPassword", salt);
        String hash2 = Mcrypt.hashWithSalt("TestPassword", salt);
        assertEquals(hash1, hash2, "Same password + salt should produce same hash");
    }

    @Test
    @DisplayName("hashWithSalt produces different hashes for different passwords")
    void hashWithSaltDifferentPasswords() {
        String salt = Mcrypt.gensalt(12);
        String hash1 = Mcrypt.hashWithSalt("PasswordA", salt);
        String hash2 = Mcrypt.hashWithSalt("PasswordB", salt);
        assertNotEquals(hash1, hash2, "Different passwords should produce different hashes");
    }

    @Test
    @DisplayName("hashWithSalt rejects null password")
    void hashWithSaltRejectsNullPassword() {
        String salt = Mcrypt.gensalt(12);
        assertThrows(IllegalArgumentException.class, () -> {
            Mcrypt.hashWithSalt(null, salt);
        }, "Null password should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("hashWithSalt rejects null salt")
    void hashWithSaltRejectsNullSalt() {
        assertThrows(IllegalArgumentException.class, () -> {
            Mcrypt.hashWithSalt("test", null);
        }, "Null salt should throw IllegalArgumentException");
    }

    // ────────── verify() Tests ──────────

    @Test
    @DisplayName("verify returns true for correct password")
    void verifyCorrectPassword() {
        String salt = Mcrypt.gensalt(12);
        String hash = Mcrypt.hashWithSalt("Gadag@2026", salt);
        assertTrue(Mcrypt.verify("Gadag@2026", hash), "Correct password should verify as true");
    }

    @Test
    @DisplayName("verify returns false for wrong password")
    void verifyWrongPassword() {
        String salt = Mcrypt.gensalt(12);
        String hash = Mcrypt.hashWithSalt("Gadag@2026", salt);
        assertFalse(Mcrypt.verify("WrongPass123", hash), "Wrong password should verify as false");
    }

    @Test
    @DisplayName("verify returns false for empty password")
    void verifyEmptyPassword() {
        String salt = Mcrypt.gensalt(12);
        String hash = Mcrypt.hashWithSalt("Gadag@2026", salt);
        assertFalse(Mcrypt.verify("", hash), "Empty password should verify as false");
    }

    @Test
    @DisplayName("verify is case-sensitive")
    void verifyIsCaseSensitive() {
        String salt = Mcrypt.gensalt(12);
        String hash = Mcrypt.hashWithSalt("Gadag@2026", salt);
        assertFalse(Mcrypt.verify("gadag@2026", hash), "Case-different password should verify as false");
    }

    @Test
    @DisplayName("verify rejects null password")
    void verifyRejectsNullPassword() {
        String salt = Mcrypt.gensalt(12);
        String hash = Mcrypt.hashWithSalt("test", salt);
        assertThrows(IllegalArgumentException.class, () -> {
            Mcrypt.verify(null, hash);
        }, "Null password should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("verify rejects null hash")
    void verifyRejectsNullHash() {
        assertThrows(IllegalArgumentException.class, () -> {
            Mcrypt.verify("test", null);
        }, "Null hash should throw IllegalArgumentException");
    }

    // ────────── Edge Cases ──────────

    @Test
    @DisplayName("handles long password correctly")
    void handlesLongPassword() {
        String salt = Mcrypt.gensalt(8);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append('A');
        String longPass = sb.toString();

        String hash = Mcrypt.hashWithSalt(longPass, salt);
        assertNotNull(hash, "Long password should hash successfully");
        assertTrue(Mcrypt.verify(longPass, hash), "Long password should verify correctly");
    }

    @Test
    @DisplayName("handles special characters in password")
    void handlesSpecialCharacters() {
        String salt = Mcrypt.gensalt(8);
        String specialPass = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String hash = Mcrypt.hashWithSalt(specialPass, salt);
        assertTrue(Mcrypt.verify(specialPass, hash), "Special chars password should verify correctly");
    }

    @Test
    @DisplayName("low rounds (4) works correctly")
    void lowRoundsWork() {
        String salt = Mcrypt.gensalt(4);
        String hash = Mcrypt.hashWithSalt("QuickTest", salt);
        assertTrue(Mcrypt.verify("QuickTest", hash), "Low rounds should work");
        assertFalse(Mcrypt.verify("WrongTest", hash), "Wrong password should fail with low rounds");
    }

    // ────────── Full E2E Flow ──────────

    @Test
    @DisplayName("complete hash-and-verify flow works")
    void completeFlowWorks() {
        // Simulate a real-world sign-up → login flow
        String password = "KoppalGadag@2026";

        // Sign-up: generate salt and hash
        String salt = Mcrypt.gensalt(12);
        String storedHash = Mcrypt.hashWithSalt(password, salt);

        // Login: verify
        assertTrue(Mcrypt.verify(password, storedHash), "Login with correct password should succeed");
        assertFalse(Mcrypt.verify("hacker123", storedHash), "Login with wrong password should fail");
    }
}
