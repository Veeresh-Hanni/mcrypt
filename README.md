# mcrypt (Mass Crypto) 🚀

`mcrypt` (Mass Cryptography) is an ultra-fast, production-ready password hashing library and CLI tool built in Rust. Inspired by bcrypt, `mcrypt` is architected from the ground up to handle **mass cryptography operations**—such as handling millions of password hashes concurrently during database migrations or high-traffic authentication workloads—without choking the server.

---

## 🔥 Key Features

* **Blazing Fast Key Stretching:** Implements a hardware-accelerated SHA-256 stretching engine running over 2^rounds iterations.
* **OS-Level CSPRNG Salting:** Uses the operating system's secure entropy (`OsRng`) to generate cryptographically secure unique salts.
* **Timing-Attack Protection:** Core verification utilizes a **Constant-Time Byte Comparison** algorithm to safely mitigate side-channel timing attacks.
* **Strict Hidden Pepper:** Supports compiled internal secrets and custom application peppers to protect against database leaks.
* **Universal FFI Bindings:** Written in Rust, exportable via C-FFI to **Python, JavaScript (Node.js), C/C++, and Java** with zero performance overhead.
* **Production CLI Integration:** Ships as a standalone native binary utility tool for sysadmins and DevOps engineers.

---

## 📊 Standard Format Layout

`mcrypt` outputs hashes in an explicit structured string layout:

```text
$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us$33a48da36f94c4c960e03...
  │      │  │   │    │                │
  │      │  │   │    └─► Secure Salt  └─► Final Hex Hash (SHA-256)
  │      │  │   └─► Salt Length (16 bytes)
  │      │  └─► Work Factor / Rounds (2^12 = 4096 iterations)
  │      └─► Core Layout Version
  └─► mcrypt Signature Identifier
```

---

## 🚀 Installation & Setup

### 🦀 Rust & CLI Installation

```bash
cargo install --path .
```

### 🐍 Python Installation

```bash
pip install mcrypt-mass-crypto
```

### 🟢 Node.js Installation

```bash
npm install mcrypt-mass-crypto
```

### ☕ Java Installation (Maven)

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.veereshhanni</groupId>
    <artifactId>mcrypt-mass-crypto-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

Or build from source:

```bash
cargo build --release
cd bindings/java
mvn package
```

At runtime, make sure the native library is on `java.library.path` or pass an explicit JNA library name/path:

```bash
java -Dmcrypt.library=../../target/release/mcrypt_native -jar your-app.jar
```

### 🔧 C/C++ Installation

Include the header and link against the native library:

```c
#include "mcrypt.h"
// Link with: -lmcrypt_native
```

---

## 🛠️ Usage Quickstart

### 1. Command Line Interface (CLI)

Always wrap your salt strings in **single quotes (`'...'`)** inside PowerShell/Terminals to prevent environment variable interpolation of the `$` tokens.

```powershell
# Step A: Generate a secure salt prefix (12 rounds)
$ mcrypt gensalt 12
$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us

# Step B: Compute the database hash using the generated salt
$ mcrypt hash 'MyPass' '$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us'
$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us$33a48da36f94c4c960e03f5b051a3aa2579d2ba4489d59bfa37787b527d2e685

# Step C: Verify credentials directly from the terminal
$ mcrypt verify 'MyPass' '$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us$33a48da36f94c4c960e03...'
true
```

### 2. Python

```python
import mcrypt

# Generate salt
salt = mcrypt.gensalt(rounds=12)

# Hash password for database storage
db_hash = mcrypt.hash_with_salt("KoppalGadag@2026", salt_prefix=salt)

# Verify user login
is_valid = mcrypt.verify("KoppalGadag@2026", db_hash)
print(f"Authentication success: {is_valid}")  # True

# With custom application pepper
db_hash = mcrypt.hash_with_salt("MyPassword", salt_prefix=salt, custom_pepper="AppSecret!")
is_valid = mcrypt.verify("MyPassword", db_hash, custom_pepper="AppSecret!")
```

### 3. Node.js

```javascript
const mcrypt = require('mcrypt-mass-crypto');

// Generate salt
const salt = mcrypt.gensalt(12, 16);

// Hash password for database storage
const dbHash = mcrypt.hashWithSalt("KoppalGadag@2026", salt);

// Verify user login
const isValid = mcrypt.verify("KoppalGadag@2026", dbHash);
console.log(`Authentication success: ${isValid}`);  // true
```

### 4. Java

```java
import io.github.veereshhanni.mcrypt.Mcrypt;

// Generate salt
String salt = Mcrypt.gensalt(12);

// Hash password for database storage
String hash = Mcrypt.hashWithSalt("KoppalGadag@2026", salt);

// Verify user login
boolean valid = Mcrypt.verify("KoppalGadag@2026", hash);
System.out.println("Authentication success: " + valid);  // true
```

### 5. C/C++

```c
#include "mcrypt.h"

// Generate salt
const char* salt = mcrypt_gensalt(12, 16);

// Hash password
const char* hash = mcrypt_hash_with_salt("KoppalGadag@2026", salt);

// Verify
bool valid = mcrypt_verify("KoppalGadag@2026", hash);

// IMPORTANT: Free strings returned by mcrypt to avoid memory leaks
mcrypt_free_string((char*)hash);
mcrypt_free_string((char*)salt);
```

---

## 🧪 Testing

mcrypt ships with comprehensive test suites for all supported languages:

```bash
# Python (28 tests)
maturin develop && python tests/test_python.py

# CLI (24 tests)
cargo build --release && powershell -File tests/test_cli.ps1

# Node.js (25 tests)
cd bindings/nodejs && npm install && cd ../.. && node tests/test_nodejs.js

# Java (27 tests)
cd bindings/java && mvn test

# Production import tests (simulates real-world pip/npm install usage)
python tests/test_production_python.py
node tests/test_production_nodejs.js
```

---

## 🛡️ Security Best Practices Enforced

1. **Work Factor Tuning:** `mcrypt` strictly enforces rounds between `4` and `31`. We recommend setting `rounds=12` for standard applications and scaling up based on server benchmarking metrics.
2. **Memory Hardening:** C-FFI layers include an explicit `mcrypt_free_string` controller to force memory de-allocation inside native execution boundaries, avoiding potential memory leaks across managed runtime ecosystems like Node.js and Java.
3. **Constant-Time Comparison:** All `verify()` functions use XOR-based constant-time byte comparison to prevent timing side-channel attacks.
4. **Application Pepper Support:** Supports custom peppers per-application, providing an additional layer of protection against database-only leaks.

---

## 📦 CI/CD & Publishing

mcrypt uses GitHub Actions for automated publishing on tag push (`v*`):

| Package | Registry | Install Command |
|---------|----------|-----------------|
| Python  | [PyPI](https://pypi.org/project/mcrypt-mass-crypto/) | `pip install mcrypt-mass-crypto` |
| Node.js | [npm](https://www.npmjs.com/package/mcrypt-mass-crypto) | `npm install mcrypt-mass-crypto` |
| Java    | Maven / GitHub Packages | See Maven dependency above |
| Rust CLI | Source | `cargo install --path .` |

---

## 📄 License

Distributed under the **MIT License**. Feel free to use, distribute, and optimize.
