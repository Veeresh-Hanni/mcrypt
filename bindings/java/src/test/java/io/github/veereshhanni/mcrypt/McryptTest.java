package io.github.veereshhanni.mcrypt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McryptTest {
    @Test
    void hashesAndVerifiesPassword() {
        String salt = Mcrypt.gensalt(12);
        String hash = Mcrypt.hashWithSalt("Gadag@2026", salt);

        assertTrue(Mcrypt.verify("Gadag@2026", hash));
        assertFalse(Mcrypt.verify("WrongPass123", hash));
    }
}
