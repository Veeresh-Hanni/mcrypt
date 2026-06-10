package io.github.veereshhanni.mcrypt;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

interface McryptNative extends Library {
    McryptNative INSTANCE = Native.load(
            loadNativeLibrary(),
            McryptNative.class
    );

    Pointer mcrypt_gensalt(int rounds, long saltLength);

    Pointer mcrypt_hash_with_salt(String password, String saltPrefix);

    byte mcrypt_verify(String password, String storedHash);

    void mcrypt_free_string(Pointer pointer);

    static String loadNativeLibrary() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        String libraryName;
        String resourcePath;
        
        if (osName.contains("win")) {
            libraryName = "mcrypt_native.dll";
            resourcePath = "windows-x86-64/mcrypt_native.dll";
        } else if (osName.contains("mac")) {
            libraryName = "libmcrypt_native.dylib";
            resourcePath = "macos-x86-64/libmcrypt_native.dylib";
        } else {
            libraryName = "libmcrypt_native.so";
            resourcePath = "linux-x86-64/libmcrypt_native.so";
        }
        
        // Try system property first
        String customPath = System.getProperty("mcrypt.library");
        if (customPath != null && !customPath.isEmpty()) {
            return customPath;
        }
        
        // Try to load from jar resources
        try {
            InputStream resourceStream = McryptNative.class.getResourceAsStream("/" + resourcePath);
            if (resourceStream != null) {
                File tempFile = File.createTempFile("mcrypt_native", osName.contains("win") ? ".dll" : ".so");
                tempFile.deleteOnExit();
                Files.copy(resourceStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return tempFile.getAbsolutePath();
            }
        } catch (IOException e) {
            // Continue to next option
        }
        
        // Fallback to system library
        return libraryName;
    }
}

