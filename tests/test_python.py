# -*- coding: utf-8 -*-
"""
mcrypt (Mass Crypto) - Python Binding Tests
============================================
Run: maturin develop && python tests/test_python.py
"""
import mcrypt
import sys
import os

# Fix Windows console encoding
if os.name == 'nt':
    sys.stdout.reconfigure(encoding='utf-8')

PASS = 0
FAIL = 0


def test(name, condition):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  [PASS] {name}")
    else:
        FAIL += 1
        print(f"  [FAIL] {name}")


def main():
    global PASS, FAIL
    print("=" * 60)
    print("  mcrypt Python Binding Test Suite")
    print("=" * 60)

    # ---------- 1. gensalt() ----------
    print("\n--- Test Group: gensalt() ---")

    salt = mcrypt.gensalt(rounds=12)
    test("gensalt returns a string", isinstance(salt, str))
    test("gensalt starts with $mcrypt$", salt.startswith("$mcrypt$"))
    test("gensalt contains version v3", "$v3$" in salt)
    test("gensalt contains round info r12", "$r12$" in salt)
    test("gensalt contains salt length sl16", "$sl16$" in salt)

    # Different rounds
    salt_4 = mcrypt.gensalt(rounds=4)
    test("gensalt rounds=4 has $r04$", "$r04$" in salt_4)

    salt_20 = mcrypt.gensalt(rounds=20)
    test("gensalt rounds=20 has $r20$", "$r20$" in salt_20)

    # Unique salts every time
    salt_a = mcrypt.gensalt(rounds=12)
    salt_b = mcrypt.gensalt(rounds=12)
    test("Two gensalt calls produce different salts", salt_a != salt_b)

    # ---------- 2. hash_with_salt() ----------
    print("\n--- Test Group: hash_with_salt() ---")

    password = "KoppalGadag@2026"
    salt = mcrypt.gensalt(rounds=12)
    hashed = mcrypt.hash_with_salt(password, salt_prefix=salt)

    test("hash_with_salt returns a string", isinstance(hashed, str))
    test("hash starts with salt prefix", hashed.startswith(salt))
    test("hash is longer than salt", len(hashed) > len(salt))

    # Hash format: $mcrypt$v3$r12$sl16$<salt>$<64-char-hex>
    parts = hashed.split("$")
    test("hash has 7 parts when split by $", len(parts) == 7)
    test("hash hex digest is 64 chars (SHA-256)", len(parts[6]) == 64)

    # Same password + same salt = same hash (deterministic)
    hashed_again = mcrypt.hash_with_salt(password, salt_prefix=salt)
    test("Same password+salt gives same hash", hashed == hashed_again)

    # Different password = different hash
    hashed_diff = mcrypt.hash_with_salt("DifferentPass", salt_prefix=salt)
    test("Different password gives different hash", hashed != hashed_diff)

    # ---------- 3. verify() ----------
    print("\n--- Test Group: verify() ---")

    is_valid = mcrypt.verify(password, hashed)
    test("Correct password verifies as True", is_valid is True)

    is_wrong = mcrypt.verify("WrongPassword123!", hashed)
    test("Wrong password verifies as False", is_wrong is False)

    is_empty = mcrypt.verify("", hashed)
    test("Empty password verifies as False", is_empty is False)

    # Case sensitivity
    is_case = mcrypt.verify("kopplgadag@2026", hashed)
    test("Case-different password verifies as False", is_case is False)

    # ---------- 4. Custom Pepper ----------
    print("\n--- Test Group: Custom Pepper ---")

    salt = mcrypt.gensalt(rounds=10)
    hash_pepper1 = mcrypt.hash_with_salt("MyPassword", salt_prefix=salt, custom_pepper="PepperA")
    hash_pepper2 = mcrypt.hash_with_salt("MyPassword", salt_prefix=salt, custom_pepper="PepperB")
    hash_default = mcrypt.hash_with_salt("MyPassword", salt_prefix=salt)

    test("Different peppers produce different hashes", hash_pepper1 != hash_pepper2)
    test("Custom pepper differs from default pepper", hash_pepper1 != hash_default)

    # Verify with matching pepper
    test("Verify works with matching custom pepper",
         mcrypt.verify("MyPassword", hash_pepper1, custom_pepper="PepperA") is True)
    test("Verify fails with wrong custom pepper",
         mcrypt.verify("MyPassword", hash_pepper1, custom_pepper="PepperB") is False)

    # ---------- 5. Edge Cases ----------
    print("\n--- Test Group: Edge Cases ---")

    # Unicode password
    salt = mcrypt.gensalt(rounds=8)
    unicode_pass = "Password123"
    unicode_hash = mcrypt.hash_with_salt(unicode_pass, salt_prefix=salt)
    test("Unicode password hashes successfully", isinstance(unicode_hash, str))
    test("Unicode password verifies correctly",
         mcrypt.verify(unicode_pass, unicode_hash) is True)

    # Long password
    long_pass = "A" * 1000
    long_hash = mcrypt.hash_with_salt(long_pass, salt_prefix=salt)
    test("Long password (1000 chars) hashes successfully", isinstance(long_hash, str))
    test("Long password verifies correctly",
         mcrypt.verify(long_pass, long_hash) is True)

    # Invalid salt format
    try:
        mcrypt.hash_with_salt("test", salt_prefix="invalid_salt")
        test("Invalid salt format raises error", False)
    except Exception:
        test("Invalid salt format raises error", True)

    # ---------- Summary ----------
    print("\n" + "=" * 60)
    total = PASS + FAIL
    print(f"  Results: {PASS}/{total} passed, {FAIL} failed")
    print("=" * 60)

    if FAIL > 0:
        sys.exit(1)
    print("\n  All Python tests passed!\n")


if __name__ == "__main__":
    main()
