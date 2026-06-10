# Release & Deployment Guide

## How the Release Workflow Works

When you push a Git tag matching `v*`, GitHub Actions automatically:

1. **Builds** native binaries for Linux, macOS, Windows (in parallel)
2. **Publishes Java** to GitHub Packages
3. **Publishes Node.js** to NPM registry
4. **Publishes Python** to PyPI

## Prerequisites

### Set up GitHub Secrets

Go to GitHub Repo → Settings → Secrets and Variables → Actions

#### 1. **For Java (GitHub Packages)**
- Already works with `${{ secrets.GITHUB_TOKEN }}` (automatic)

#### 2. **For Node.js (NPM)**
Add: `NPM_TOKEN`
- Get token from https://www.npmjs.com/settings/YOUR_USERNAME/tokens
- Select **"Granular Access Token"**
- Permissions:
  - **Packages**: Read and write
  - **Organizations**: Read only
- Save token to GitHub Secrets as `NPM_TOKEN`

#### 3. **For Python (PyPI)**
Add: `PYPI_TOKEN`
- Get token from https://pypi.org/account/
- Go to API tokens → Create token
- Scope: **Entire account** (or specific project)
- Save token to GitHub Secrets as `PYPI_TOKEN`

## Release Process

### Step 1: Update Version Numbers
Update version in:
- `Cargo.toml` (Rust)
- `Cargo.lock` (auto-generated)
- `pyproject.toml` (Python)
- `bindings/nodejs/package.json` (Node.js)
- `bindings/java/pom.xml` (Java)

```bash
# Example: Update to 1.2.1
# Edit all files above to version: 1.2.1
```

### Step 2: Commit & Tag
```bash
git add .
git commit -m "release: version 1.2.1"
git tag v1.2.1
git push origin main
git push origin v1.2.1
```

### Step 3: Monitor Release
Go to GitHub Repo → Actions → Watch the workflow run

**Workflow runs 3 jobs in parallel:**
- `publish-java` → GitHub Packages
- `publish-nodejs` → NPM
- `publish-python` → PyPI

## Verify Packages Published

### Java (GitHub Packages)
```bash
# Maven dependency
<dependency>
    <groupId>io.github.veeresh-hanni</groupId>
    <artifactId>mcrypt-mass-crypto-java</artifactId>
    <version>1.2.1</version>
</dependency>

# Gradle dependency
implementation 'io.github.veeresh-hanni:mcrypt-mass-crypto-java:1.2.1'

# View at:
https://github.com/Veeresh-Hanni/mcrypt/packages
```

### Node.js (NPM)
```bash
npm install mcrypt-mass-crypto@1.2.1

# View at:
https://www.npmjs.com/package/mcrypt-mass-crypto
```

### Python (PyPI)
```bash
pip install mcrypt-mass-crypto==1.2.1

# View at:
https://pypi.org/project/mcrypt-mass-crypto/
```

## Troubleshooting

### Java Publishing Fails
- Check if `GITHUB_TOKEN` has package write permissions (usually automatic)
- Verify pom.xml `distributionManagement` points to GitHub Packages
- Check: GitHub Repo → Settings → Actions → General → Workflow permissions

### Node.js Publishing Fails
- Verify `NPM_TOKEN` is set and valid
- Check token has `write:packages` permission
- Try: `npm publish --dry-run` locally to test

### Python Publishing Fails
- Verify `PYPI_TOKEN` is set and valid
- Check token hasn't expired
- Ensure version isn't already published (can't republish same version)
- For test: set version to `1.2.1rc1` (release candidate)

### Binary Build Fails on Specific Platform
- All platform builds run in parallel
- If one fails, others continue (fail-fast: false)
- Check workflow logs for specific OS error
- Common issues:
  - macOS: Xcode not installed
  - Windows: MSVC toolchain missing
  - Linux: Build tools not found

## Local Testing Before Release

### Test Java Locally
```bash
cd bindings/java
mvn clean package
mvn install  # Install to local repo
```

### Test Node.js Locally
```bash
cd bindings/nodejs
npm install
npm pack  # Creates .tgz file to inspect
node -e "const m = require('.'); console.log(m.gensalt(12, 16))"
```

### Test Python Locally
```bash
pip install maturin
maturin develop  # Build and install locally
python -c "import mcrypt; print(mcrypt.gensalt(12, 16))"
```

## Rollback Released Version

**Note:** Cannot delete published packages, but can:

1. **For NPM:** Deprecate version
   ```bash
   npm deprecate mcrypt-mass-crypto@1.2.1 "Use 1.2.2 instead"
   ```

2. **For PyPI:** Yank version
   - Go to PyPI project → Version history → Yank version

3. **For Java:** Mark as deprecated in documentation

## CI/CD Workflow File

Location: `.github/workflows/release.yml`

Key jobs:
- `build-native`: Builds binaries for all platforms (Linux/macOS/Windows)
- `publish-java`: Assembles JAR with binaries and publishes to GitHub Packages
- `publish-nodejs`: Copies binaries to npm package and publishes
- `publish-python`: Builds wheels and publishes to PyPI

All publish jobs depend on `build-native` completing first.

## Environment Variables Needed

### GitHub Actions Automatically Provides
- `GITHUB_TOKEN` - Automatically available for GitHub Packages

### You Must Add to Secrets
- `NPM_TOKEN` - For NPM publishing
- `PYPI_TOKEN` - For PyPI publishing

## Tips

✅ **Always test locally first** before pushing a tag
✅ **Use `-rc` (release candidate) versions** for testing: `1.2.1-rc1`
✅ **Tag after commits are tested**, not before
✅ **Monitor workflow runs** - click Actions tab after pushing tag
✅ **Keep version numbers in sync** across all `package.json`, `pom.xml`, `pyproject.toml`, `Cargo.toml`
