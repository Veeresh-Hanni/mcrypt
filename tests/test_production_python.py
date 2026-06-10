# -*- coding: utf-8 -*-
"""
mcrypt Production Test - Python (PyPI)
=======================================
Install: pip install mcrypt-mass-crypto
Run:     python tests/test_production_python.py

This test simulates how end-users import and use mcrypt
after installing from PyPI via: pip install mcrypt-mass-crypto
"""
import sys
import os

if os.name == 'nt':
    sys.stdout.reconfigure(encoding='utf-8')

# ============================================================
# Production Import - exactly how users import mcrypt
# ============================================================
import mcrypt

print("=" * 60)
print("  mcrypt Production Test (Python / PyPI)")
print("  Package: mcrypt-mass-crypto")
print("=" * 60)

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


# ---------- 1. User Registration Flow ----------
print("\n--- Scenario: User Registration ---")

password = "MySecureP@ssw0rd!2026"

# Step 1: Generate salt (server-side on sign-up)
salt = mcrypt.gensalt(rounds=12)
test("gensalt() returns valid salt", salt.startswith("$mcrypt$v3$"))
print(f"    Salt: {salt}")

# Step 2: Hash password before storing in DB
stored_hash = mcrypt.hash_with_salt(password, salt_prefix=salt)
test("hash_with_salt() returns hash", len(stored_hash) > len(salt))
print(f"    Hash: {stored_hash}")

# ---------- 2. User Login Flow ----------
print("\n--- Scenario: User Login ---")

# Correct password login
login_result = mcrypt.verify(password, stored_hash)
test("Login with correct password -> True", login_result is True)

# Wrong password login attempt
hacker_result = mcrypt.verify("hacker123", stored_hash)
test("Login with wrong password -> False", hacker_result is False)

# Brute force attempt with similar password
similar_result = mcrypt.verify("MySecureP@ssw0rd!2027", stored_hash)
test("Login with similar password -> False", similar_result is False)

# Empty password attempt
empty_result = mcrypt.verify("", stored_hash)
test("Login with empty password -> False", empty_result is False)

# ---------- 3. Password Change Flow ----------
print("\n--- Scenario: Password Change ---")

new_password = "NewStr0ngerP@ss!2026"
new_salt = mcrypt.gensalt(rounds=12)
new_hash = mcrypt.hash_with_salt(new_password, salt_prefix=new_salt)

# Old password should no longer work
old_login = mcrypt.verify(password, new_hash)
test("Old password fails against new hash -> False", old_login is False)

# New password should work
new_login = mcrypt.verify(new_password, new_hash)
test("New password works against new hash -> True", new_login is True)

# ---------- 4. Custom Pepper (Application Secret) ----------
print("\n--- Scenario: Application Pepper ---")

app_pepper = "MyApp_Secret_Pepper_2026!"
salt = mcrypt.gensalt(rounds=10)

hash_with_pepper = mcrypt.hash_with_salt("UserPass", salt_prefix=salt, custom_pepper=app_pepper)
hash_without_pepper = mcrypt.hash_with_salt("UserPass", salt_prefix=salt)

test("Pepper changes the hash output", hash_with_pepper != hash_without_pepper)

# Verify with correct pepper
test("Verify with correct pepper -> True",
     mcrypt.verify("UserPass", hash_with_pepper, custom_pepper=app_pepper) is True)

# Verify with wrong pepper fails
test("Verify with wrong pepper -> False",
     mcrypt.verify("UserPass", hash_with_pepper, custom_pepper="WrongPepper") is False)

# ---------- 5. Batch Hashing (Mass Crypto) ----------
print("\n--- Scenario: Batch Hashing (5 users) ---")

users = [
    ("alice", "Alice@Pass123"),
    ("bob", "B0bSecure!"),
    ("charlie", "Ch@rlie2026"),
    ("diana", "D1ana_Strong"),
    ("eve", "Ev3_P@ssword"),
]

hashes = {}
for username, pwd in users:
    s = mcrypt.gensalt(rounds=10)
    h = mcrypt.hash_with_salt(pwd, salt_prefix=s)
    hashes[username] = h

# Verify all users
all_valid = all(mcrypt.verify(pwd, hashes[user]) for user, pwd in users)
test("All 5 users hash and verify correctly", all_valid)

# Cross-verify: alice's password should NOT match bob's hash
cross_check = mcrypt.verify("Alice@Pass123", hashes["bob"])
test("Alice's password fails against Bob's hash", cross_check is False)

# ---------- 6. API Completeness Check ----------
print("\n--- Scenario: API Completeness ---")

test("mcrypt.gensalt exists", hasattr(mcrypt, 'gensalt'))
test("mcrypt.hash_with_salt exists", hasattr(mcrypt, 'hash_with_salt'))
test("mcrypt.verify exists", hasattr(mcrypt, 'verify'))

# ---------- Summary ----------
print("\n" + "=" * 60)
total = PASS + FAIL
print(f"  Results: {PASS}/{total} passed, {FAIL} failed")
print("=" * 60)

if FAIL > 0:
    print("\n  PRODUCTION TEST FAILED!")
    sys.exit(1)

print("\n  All production tests passed!")
print("  mcrypt is ready for production use.\n")
