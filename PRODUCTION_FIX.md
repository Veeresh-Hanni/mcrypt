# Production Issue Fix: Binary Distribution for Node.js & Java

## Problem
Node.js and Java bindings were failing in production because system binary files (.so, .dll, .dylib) were not being bundled with the packages, while Python worked fine.

**Root Causes:**
1. **Node.js**: Hardcoded path to `target/release/` (development-only path)
2. **Java**: Binaries not included in JAR, CI only built for Linux
3. **Python**: Already working because `maturin` handles binary packaging

## Solutions Implemented

### 1. Updated `.gitignore`
- Blocks binaries at root level (as before)
- **Allows** binaries in package directories:
  - `bindings/java/src/main/resources/**/*.{so,dll,dylib}`
  - `bindings/nodejs/lib/**/*.{so,dll,dylib}`

### 2. Node.js Binding Fixes (`bindings/nodejs/`)

**index.js:**
- Try to load bundled binary from `lib/` directory first (production)
- Fallback to `target/release/` for development

**package.json:**
- Added `"files"` array to include `lib/` in npm package
- Added `"build"` script to copy binaries from `target/release/`

**scripts/copy-binaries.js:**
- Copies all platform binaries (.so, .dll, .dylib) to `lib/` for packaging

### 3. Java Binding Fixes (`bindings/java/`)

**McryptNative.java:**
- New `loadNativeLibrary()` method that:
  - Detects OS (Windows/macOS/Linux)
  - Extracts binary from JAR resources at runtime
  - Falls back to system library if not found
  - Respects `mcrypt.library` system property for custom paths

**pom.xml:**
- Added `maven-antrun-plugin` to copy binaries from `target/release/` to resource directories
- Updated resource copying to handle all platforms (Linux, macOS, Windows)
- JAR now includes all binaries in `src/main/resources/{linux-x86-64,macos-x86-64,windows-x86-64}/`

### 4. CI/CD Pipeline (`release.yml`)

**New Structure:**
1. **build-native** job: Build binaries for all platforms (Linux, macOS, Windows) in parallel
2. **publish-all** job: Download and organize all binaries
3. **publish-python**: Uses organized binaries
4. **publish-java**: Includes all platform binaries in JAR
5. **publish-nodejs**: Bundles all platform binaries in npm package

**Key Improvements:**
- Cross-platform binary compilation (Linux, macOS, Windows)
- Binaries shared across all package formats
- All platform binaries included in JAR and npm package

## How It Works Now

### Development
```bash
# Build native library
cargo build --release

# Node.js (tries lib/ first, falls back to target/release/)
cd bindings/nodejs
npm install
npm run build  # Copies binaries to lib/

# Java (will use target/release/ as fallback)
cd bindings/java
mvn clean package

# Python (as before)
cd ../..
pip install maturin
maturin build --release
```

### Production (from npm/Maven)
- **Node.js**: Loads bundled binary from `lib/` directory
- **Java**: Extracts binary from JAR resources at runtime
- **Python**: Already handled by wheel mechanism

## Testing Production Packages

```bash
# Test Node.js package (after npm install)
const mcrypt = require('mcrypt-mass-crypto');
console.log(mcrypt.gensalt(12, 16));

# Test Java (after Maven install)
java -cp target/mcrypt-mass-crypto-java-1.2.0.jar:. \
  io.github.veereshhanni.mcrypt.Mcrypt
```

## Resource Directories Structure

```
bindings/
├── java/src/main/resources/
│   ├── linux-x86-64/
│   │   └── libmcrypt_native.so
│   ├── macos-x86-64/
│   │   └── libmcrypt_native.dylib
│   └── windows-x86-64/
│       └── mcrypt_native.dll
└── nodejs/lib/
    ├── libmcrypt_native.so
    ├── libmcrypt_native.dylib
    └── mcrypt_native.dll
```

All directories contain `.gitkeep` files to ensure they're tracked by git and can be populated during CI/CD builds.
