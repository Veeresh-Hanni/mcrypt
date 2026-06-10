/**
 * mcrypt (Mass Crypto) - C/C++ FFI Binding Tests
 * ================================================
 * 
 * Compile (Windows):
 *   cl /I ../bindings/c_cpp tests/test_c.c /link /LIBPATH:target/release mcrypt_native.dll.lib /OUT:test_c.exe
 * 
 * Compile (Linux/Mac):
 *   gcc -I bindings/c_cpp tests/test_c.c -L target/release -lmcrypt_native -o test_c
 *   LD_LIBRARY_PATH=target/release ./test_c
 */

#include <stdio.h>
#include <string.h>
#include <stdbool.h>
#include "mcrypt.h"

static int PASS = 0;
static int FAIL = 0;

void test(const char* name, bool condition) {
    if (condition) {
        PASS++;
        printf("  ✅ PASS: %s\n", name);
    } else {
        FAIL++;
        printf("  ❌ FAIL: %s\n", name);
    }
}

int main() {
    printf("============================================================\n");
    printf("  mcrypt C/C++ FFI Binding Test Suite\n");
    printf("============================================================\n");

    /* ────────── 1. gensalt ────────── */
    printf("\n📌 Test Group: gensalt()\n");

    const char* salt = mcrypt_gensalt(12, 16);
    test("gensalt returns non-null", salt != NULL);
    test("gensalt starts with $mcrypt$", strncmp(salt, "$mcrypt$", 8) == 0);
    test("gensalt contains $v3$", strstr(salt, "$v3$") != NULL);
    test("gensalt contains $r12$", strstr(salt, "$r12$") != NULL);
    test("gensalt contains $sl16$", strstr(salt, "$sl16$") != NULL);

    /* Copy salt before freeing for later use */
    char salt_copy[256];
    strncpy(salt_copy, salt, sizeof(salt_copy) - 1);
    salt_copy[sizeof(salt_copy) - 1] = '\0';
    mcrypt_free_string((char*)salt);

    /* Different rounds */
    const char* salt4 = mcrypt_gensalt(4, 16);
    test("gensalt rounds=4 has $r04$", strstr(salt4, "$r04$") != NULL);
    mcrypt_free_string((char*)salt4);

    /* Unique salts */
    const char* saltA = mcrypt_gensalt(12, 16);
    const char* saltB = mcrypt_gensalt(12, 16);
    test("Two gensalt calls produce different salts", strcmp(saltA, saltB) != 0);
    mcrypt_free_string((char*)saltA);
    mcrypt_free_string((char*)saltB);

    /* ────────── 2. hash_with_salt ────────── */
    printf("\n📌 Test Group: hash_with_salt()\n");

    const char* password = "KoppalGadag@2026";
    const char* hashed = mcrypt_hash_with_salt(password, salt_copy);

    test("hash_with_salt returns non-null", hashed != NULL);
    test("hash starts with salt prefix", strncmp(hashed, salt_copy, strlen(salt_copy)) == 0);
    test("hash is longer than salt", strlen(hashed) > strlen(salt_copy));

    char hash_copy[512];
    strncpy(hash_copy, hashed, sizeof(hash_copy) - 1);
    hash_copy[sizeof(hash_copy) - 1] = '\0';
    mcrypt_free_string((char*)hashed);

    /* Deterministic */
    const char* hashed_again = mcrypt_hash_with_salt(password, salt_copy);
    test("Same password+salt gives same hash", strcmp(hash_copy, hashed_again) == 0);
    mcrypt_free_string((char*)hashed_again);

    /* Different password */
    const char* hashed_diff = mcrypt_hash_with_salt("DifferentPass", salt_copy);
    test("Different password gives different hash", strcmp(hash_copy, hashed_diff) != 0);
    mcrypt_free_string((char*)hashed_diff);

    /* ────────── 3. verify ────────── */
    printf("\n📌 Test Group: verify()\n");

    bool is_valid = mcrypt_verify(password, hash_copy);
    test("Correct password verifies as true", is_valid == true);

    bool is_wrong = mcrypt_verify("WrongPassword123!", hash_copy);
    test("Wrong password verifies as false", is_wrong == false);

    bool is_empty = mcrypt_verify("", hash_copy);
    test("Empty password verifies as false", is_empty == false);

    /* ────────── 4. Edge Cases ────────── */
    printf("\n📌 Test Group: Edge Cases\n");

    /* NULL safety */
    const char* null_hash = mcrypt_hash_with_salt(NULL, salt_copy);
    test("NULL password returns null", null_hash == NULL);

    bool null_verify = mcrypt_verify(NULL, hash_copy);
    test("NULL password verify returns false", null_verify == false);

    bool null_hash_verify = mcrypt_verify(password, NULL);
    test("NULL hash verify returns false", null_hash_verify == false);

    /* Memory management */
    mcrypt_free_string(NULL);
    test("mcrypt_free_string(NULL) does not crash", true);

    /* Invalid hash format */
    bool bad_verify = mcrypt_verify("test", "not_a_valid_hash");
    test("Invalid hash format returns false", bad_verify == false);

    /* ────────── Summary ────────── */
    printf("\n============================================================\n");
    int total = PASS + FAIL;
    printf("  Results: %d/%d passed, %d failed\n", PASS, total, FAIL);
    printf("============================================================\n");

    if (FAIL > 0) {
        printf("\n💥 Some tests failed!\n");
        return 1;
    }
    printf("\n🎉 All C/C++ tests passed!\n\n");
    return 0;
}
