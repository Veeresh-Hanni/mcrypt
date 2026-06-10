
```markdown
# mcrypt (Mass Crypto) 🚀

`mcrypt` (Mass Cryptography) is an ultra-fast, production-ready password hashing library and CLI tool built in Rust. Inspired by bcrypt, `mcrypt` is architected from the ground up to handle **mass cryptography operations**—such as handling millions of password hashes concurrently during database migrations or high-traffic authentication workloads—without choking the server.

---

## 🔥 Key Features

* **Blazing Fast Key Stretching:** Implements a hardware-accelerated SHA-256 stretching engine running over $2^{\text{rounds}}$ iterations.
* **OS-Level CSPRNG Salting:** Uses the operating system's secure entropy (`OsRng`) to generate cryptographically secure unique salts.
* **Timing-Attack Protection:** Core verification utilizes a **Constant-Time Byte Comparison** algorithm to safely mitigate side-channel timing attacks.
* **Strict Hidden Pepper:** Supports compiled internal secrets and custom application peppers to protect against database leaks.
* **Universal FFI Bindings:** Written in Rust, exportable via C-FFI to **Python, JavaScript (Node.js), C++, and Java** with zero performance overhead.
* **Production CLI Integration:** Ships as a standalone native binary utility tool for sysadmins and DevOps engineers.

---

## 📊 Standard Format Layout

`mcrypt` outputs hashes in an explicit structured string layout:

```text
$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us$33a48da36f94c4c960e03...
  │      │  │   │    │                │
  │      │  │   │    └─► Secure Salt  └─► Final Hex Hash
  │      │  │   └─► Salt Length (16 bytes)
  │      │  └─► Work Factor / Rounds ($2^{12}$)
  │      └─► Core Layout Version
  └─► mcrypt Signature Identifier

```

---

## 🚀 Installation & Setup

### 🦀 Rust & CLI Installation

To install the standalone command-line tool globally on your system:

```bash
cargo install --path .

```

### 🐍 Python Installation

```bash
pip install mcrypt

```

### 🟢 Node.js Installation

```bash
npm install mcrypt-mass-crypto

```

### Java Installation

Build the native library first, then build the Java bindings:

```bash
cargo build --release
cd bindings/java
mvn package
```

At runtime, make sure the native library is on `java.library.path` or pass an explicit JNA library name/path:

```bash
java -Dmcrypt.library=../../target/release/mcrypt_native -jar your-app.jar
```

---

## 🛠️ Usage Quickstart

### 1. Using the Command Line Interface (CLI)

Always wrap your salt strings in **single quotes (`'...'`)** inside PowerShell/Terminals to prevent environment variable interpolation of the `$` tokens.

```powershell
# Step A: Generate a secure salt prefix (12 rounds)
$ mcrypt gensalt 12
$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us

# Step B: Compute the database hash using the generated salt
$ mcrypt hash 'MyPass' '$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us'
$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us$33a48da36f94c4c960e03f5b051a3aa2579d2ba4489d59bfa37787b527d2e685

# Step C: Verify credentials directly from the terminal
$ mcrypt verify 'MyPass' '$mcrypt$v3$r12$sl16$AGIDzaexHggeY8us$33a48da36f94c4c960e03f5b051a3aa2579d2ba4489d59bfa37787b527d2e685'
true

```

### 2. Using in Python Applications

```python
import mcrypt

# Generate architectural prefix
salt = mcrypt.gensalt(rounds=12)

# Commit hash to database storage
db_hash = mcrypt.hash_with_salt("KoppalGadag@2026", salt_prefix=salt)

# Inbound user verification pipeline
is_valid = mcrypt.verify("KoppalGadag@2026", db_hash)
print(f"Authentication success: {is_valid}") # Returns: True

```

### 3. Using in Java Applications

```java
import io.github.veereshhanni.mcrypt.Mcrypt;

String salt = Mcrypt.gensalt(12);
String hash = Mcrypt.hashWithSalt("KoppalGadag@2026", salt);
boolean valid = Mcrypt.verify("KoppalGadag@2026", hash);

System.out.println("Authentication success: " + valid);
```

---

## 🛡️ Security Best Practices Enforced

1. **Work Factor Tuning:** `mcrypt` strictly enforces rounds between `4` and `31`. We recommend setting `rounds=12` for standard applications and scaling up based on server benchmarking metrics.
2. **Memory Hardening:** C-FFI layers include an explicit `mcrypt_free_string` controller to force memory de-allocation inside native execution boundaries, avoiding potential memory leaks across managed runtime ecosystems like Node.js and Java.

---

## 📄 License

Distributed under the **MIT License**. Feel free to use, distribute, and optimize.
