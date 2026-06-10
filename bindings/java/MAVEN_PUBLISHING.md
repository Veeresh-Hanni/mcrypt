# Maven Publishing Guide

## Option 1: Maven Central (Recommended)

### Step 1: Create OSSRH Account
1. Sign up at [Sonatype JIRA](https://issues.sonatype.org/secure/Signup!default.jspa)
2. Create a ticket requesting namespace `io.github.veereshhanni` if not already created
3. Get your username and password

### Step 2: Set Up GPG Signing
```bash
# Install GPG (if not already installed)
# Windows: https://www.gpg4win.org/
# macOS: brew install gnupg
# Linux: apt-get install gnupg

# Generate GPG key
gpg --full-generate-key

# List keys (copy the KEY_ID)
gpg --list-keys

# Export public key to keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

### Step 3: Create `~/.m2/settings.xml`

**Windows:** `C:\Users\<YourUsername>\.m2\settings.xml`
**Mac/Linux:** `~/.m2/settings.xml`

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <servers>
    <server>
      <id>ossrh</id>
      <username>YOUR_SONATYPE_USERNAME</username>
      <password>YOUR_SONATYPE_PASSWORD</password>
    </server>
  </servers>

  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.passphrase>YOUR_GPG_PASSPHRASE</gpg.passphrase>
        <gpg.keyname>YOUR_GPG_KEY_ID</gpg.keyname>
      </properties>
    </profile>
  </profiles>

</settings>
```

### Step 4: Publish to Maven Central

```bash
cd bindings/java

# Deploy to OSSRH Staging
mvn clean deploy

# Log into https://s01.oss.sonatype.org/
# Navigate to Staging Repositories
# Find your repository, verify contents
# Click "Release" to promote to Maven Central
```

## Option 2: GitHub Packages (Easier Setup)

### Step 1: Create GitHub Personal Access Token
1. Go to Settings → Developer Settings → Personal Access Tokens
2. Create token with `write:packages` scope
3. Copy token

### Step 2: Update `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

### Step 3: Uncomment in `pom.xml`
```xml
<distributionManagement>
    <repository>
        <id>github</id>
        <name>GitHub Packages</name>
        <url>https://maven.pkg.github.com/Veeresh-Hanni/mcrypt</url>
    </repository>
</distributionManagement>
```

### Step 4: Publish
```bash
cd bindings/java
mvn clean deploy
```

## Option 3: GitHub Actions CI/CD

### Release Workflow
Add to `.github/workflows/release.yml`:

```yaml
publish-java:
  name: Build & Publish Java (Maven)
  runs-on: ubuntu-latest
  needs: build-native
  steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 11
      uses: actions/setup-java@v4
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Download Binaries
      uses: actions/download-artifact@v4
      with:
        path: ./binaries
    
    - name: Organize Binaries
      run: |
        mkdir -p bindings/java/src/main/resources/{linux-x86-64,macos-x86-64,windows-x86-64}
        cp binaries/binary-x86_64-unknown-linux-gnu/libmcrypt_native.so bindings/java/src/main/resources/linux-x86-64/
        cp binaries/binary-x86_64-apple-darwin/libmcrypt_native.dylib bindings/java/src/main/resources/macos-x86-64/
        cp binaries/binary-x86_64-pc-windows-msvc/mcrypt_native.dll bindings/java/src/main/resources/windows-x86-64/
    
    - name: Publish to Maven Central
      run: |
        cd bindings/java
        mvn clean deploy -P ossrh
      env:
        OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
        OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
        GPG_PASSPHRASE: ${{ secrets.GPG_PASSPHRASE }}
        GPG_KEYNAME: ${{ secrets.GPG_KEYNAME }}
```

### Set GitHub Secrets
1. Go to Settings → Secrets and Variables → Actions
2. Add:
   - `OSSRH_USERNAME`
   - `OSSRH_PASSWORD`
   - `GPG_PASSPHRASE`
   - `GPG_KEYNAME`

## Publishing Commands

### Build and Test Locally
```bash
cd bindings/java
mvn clean package
```

### Deploy to Staging (Maven Central)
```bash
mvn clean deploy -P ossrh
```

### Deploy to GitHub Packages
```bash
mvn clean deploy -P github
```

### Deploy to Local Repository (for testing)
```bash
mvn clean install
```

## Verification

### Maven Central
After deploying, verify at:
- [https://search.maven.org/artifact/io.github.veereshhanni/mcrypt-mass-crypto-java](https://search.maven.org/artifact/io.github.veereshhanni/mcrypt-mass-crypto-java)

### GitHub Packages
Verify at:
- [https://github.com/Veeresh-Hanni/mcrypt/packages](https://github.com/Veeresh-Hanni/mcrypt/packages)

## Using Published Package

### Maven
```xml
<dependency>
    <groupId>io.github.veereshhanni</groupId>
    <artifactId>mcrypt-mass-crypto-java</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle
```gradle
dependencies {
    implementation 'io.github.veereshhanni:mcrypt-mass-crypto-java:1.2.0'
}
```

## Troubleshooting

### GPG Issues
```bash
# Test GPG signing
gpg --list-keys
gpg --sign-key YOUR_KEY_ID

# Re-export key if needed
gpg --armor --export KEY_ID > public.key
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

### Maven Settings Issues
```bash
# Verify settings.xml is read
mvn help:active-profiles

# Test connection
mvn deploy:help
```

### Artifact Already Exists
First-time releases take 10 minutes to sync. Don't re-deploy the same version.

For snapshots (development), use version `1.2.0-SNAPSHOT` and deploy frequently.
