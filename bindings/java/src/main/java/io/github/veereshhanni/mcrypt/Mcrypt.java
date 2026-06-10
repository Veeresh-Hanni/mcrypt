package io.github.veereshhanni.mcrypt;

import com.sun.jna.Pointer;

public final class Mcrypt {
    private static final long DEFAULT_SALT_LENGTH = 16;

    private Mcrypt() {
    }

    public static String gensalt(int rounds) {
        return gensalt(rounds, DEFAULT_SALT_LENGTH);
    }

    public static String gensalt(int rounds, long saltLength) {
        return copyAndFree(McryptNative.INSTANCE.mcrypt_gensalt(rounds, saltLength));
    }

    public static String hashWithSalt(String password, String saltPrefix) {
        requireNonNull(password, "password");
        requireNonNull(saltPrefix, "saltPrefix");
        return copyAndFree(McryptNative.INSTANCE.mcrypt_hash_with_salt(password, saltPrefix));
    }

    public static boolean verify(String password, String storedHash) {
        requireNonNull(password, "password");
        requireNonNull(storedHash, "storedHash");
        return McryptNative.INSTANCE.mcrypt_verify(password, storedHash) != 0;
    }

    private static String copyAndFree(Pointer pointer) {
        if (pointer == null || Pointer.nativeValue(pointer) == 0) {
            throw new McryptException("mcrypt native function returned null");
        }

        try {
            return pointer.getString(0);
        } finally {
            McryptNative.INSTANCE.mcrypt_free_string(pointer);
        }
    }

    private static void requireNonNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " must not be null");
        }
    }
}
