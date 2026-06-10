package io.github.veereshhanni.mcrypt;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

interface McryptNative extends Library {
    McryptNative INSTANCE = Native.load(
            System.getProperty("mcrypt.library", "mcrypt_native"),
            McryptNative.class
    );

    Pointer mcrypt_gensalt(int rounds, long saltLength);

    Pointer mcrypt_hash_with_salt(String password, String saltPrefix);

    byte mcrypt_verify(String password, String storedHash);

    void mcrypt_free_string(Pointer pointer);
}
