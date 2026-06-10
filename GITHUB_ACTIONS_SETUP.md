# GitHub Actions Secrets Setup

## For Java Publishing to GitHub Packages

### Step 1: Create Personal Access Token

1. Go to GitHub → Settings → Developer Settings → Personal Access Tokens → **Tokens (classic)**
2. Click **"Generate new token"**
3. Select **Scopes**:
   - ✓ `write:packages`
   - ✓ `read:packages`
   - ✓ `repo` (optional, for full control)
4. Click **Generate token**
5. **Copy the token** (you won't see it again!)

### Step 2: Add to Repository Secrets

1. Go to your repository: https://github.com/Veeresh-Hanni/mcrypt
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **"New repository secret"**
4. **Name**: `ACCESS_TOKEN`
5. **Value**: Paste the token you copied
6. Click **"Add secret"**

### Step 3: Done! ✅

The workflow will now automatically use `ACCESS_TOKEN` to publish to GitHub Packages when you push a tag.

---

## For Node.js Publishing to NPM

### Create NPM Token

1. Go to https://www.npmjs.com/settings/YOUR_USERNAME/tokens
2. Click **"Generate New Token"**
3. Select **"Granular Access Token"**
4. Permissions:
   - **Packages**: Read and write
   - **Organizations**: Read only
5. Copy token

### Add to Repository Secrets

1. In your repo: Settings → Secrets and variables → Actions
2. Click **"New repository secret"**
3. **Name**: `NPM_TOKEN`
4. **Value**: Paste NPM token
5. Click **"Add secret"**

---

## For Python Publishing to PyPI

### Create PyPI Token

1. Go to https://pypi.org/account/
2. Click **API tokens** → **Add API token**
3. Scope: **Entire account** (or specific project)
4. Copy token

### Add to Repository Secrets

1. In your repo: Settings → Secrets and variables → Actions
2. Click **"New repository secret"**
3. **Name**: `PYPI_TOKEN`
4. **Value**: Paste PyPI token
5. Click **"Add secret"**

---

## Testing the Setup

### Test Java Publishing

```bash
git tag v1.2.1-test
git push origin v1.2.1-test
```

Then watch: https://github.com/Veeresh-Hanni/mcrypt/actions

---

## Allowed Secret Names (GitHub Restrictions)

❌ **NOT allowed** (reserved):
- `GITHUB_*` (all GITHUB_ prefixed names)

✅ **Allowed**:
- `ACCESS_TOKEN` ✓
- `NPM_TOKEN` ✓
- `PYPI_TOKEN` ✓
- `MY_SECRET` ✓
- Any custom name without GITHUB_ prefix ✓

---

## Current Workflow Secrets Used

| Secret | Used By | Scope |
|--------|---------|-------|
| `ACCESS_TOKEN` | Java (Maven) | GitHub Packages write |
| `NPM_TOKEN` | Node.js | NPM registry publish |
| `PYPI_TOKEN` | Python | PyPI publish |

All secrets are encrypted and only used by the GitHub Actions workflow.
