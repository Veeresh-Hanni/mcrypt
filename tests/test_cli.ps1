# mcrypt (Mass Crypto) - CLI Test Suite
# ======================================
# Run: cargo build --release && powershell -ExecutionPolicy Bypass -File tests/test_cli.ps1

$ErrorActionPreference = "Continue"

$mcrypt = "d:\mcrypt\target\release\mcrypt.exe"
if (-not (Test-Path $mcrypt)) {
    $mcrypt = "d:\mcrypt\target\debug\mcrypt.exe"
}
if (-not (Test-Path $mcrypt)) {
    Write-Host "ERROR: mcrypt binary not found. Run 'cargo build --release' first." -ForegroundColor Red
    exit 1
}

$PASS = 0
$FAIL = 0

function Test-Result($name, $condition) {
    if ($condition) {
        $script:PASS++
        Write-Host "  [PASS] $name" -ForegroundColor Green
    } else {
        $script:FAIL++
        Write-Host "  [FAIL] $name" -ForegroundColor Red
    }
}

Write-Host ("=" * 60)
Write-Host "  mcrypt CLI Test Suite"
Write-Host ("=" * 60)

# ---------- 1. gensalt ----------
Write-Host "`n--- Test Group: gensalt ---"

$salt = & $mcrypt gensalt 12
Test-Result "gensalt returns output" ($salt.Length -gt 0)
Test-Result "gensalt starts with dollar-mcrypt-dollar" ($salt.StartsWith('$mcrypt$'))
Test-Result "gensalt contains dollar-v3-dollar" ($salt.Contains('$v3$'))
Test-Result "gensalt contains dollar-r12-dollar" ($salt.Contains('$r12$'))
Test-Result "gensalt contains dollar-sl16-dollar" ($salt.Contains('$sl16$'))

# Different rounds
$salt4 = & $mcrypt gensalt 4
Test-Result "gensalt rounds=4 has dollar-r04-dollar" ($salt4.Contains('$r04$'))

# Unique salts
$saltA = & $mcrypt gensalt 12
$saltB = & $mcrypt gensalt 12
Test-Result "Two gensalt calls produce different salts" ($saltA -ne $saltB)

# Default rounds (no arg)
$saltDefault = & $mcrypt gensalt
Test-Result "gensalt with default rounds works" ($saltDefault.StartsWith('$mcrypt$'))

# ---------- 2. hash ----------
Write-Host "`n--- Test Group: hash ---"

$password = "KoppalGadag@2026"
$hashSalt = & $mcrypt gensalt 12
$hashed = & $mcrypt hash $password $hashSalt

Test-Result "hash returns output" ($hashed.Length -gt 0)
Test-Result "hash starts with salt prefix" ($hashed.StartsWith($hashSalt))
Test-Result "hash is longer than salt" ($hashed.Length -gt $hashSalt.Length)

# Format check
$parts = $hashed.Split('$')
Test-Result "hash has 7 parts when split by dollar" ($parts.Count -eq 7)
Test-Result "hash hex digest is 64 chars" ($parts[6].Length -eq 64)

# Deterministic
$hashedAgain = & $mcrypt hash $password $hashSalt
Test-Result "Same password+salt gives same hash" ($hashed -eq $hashedAgain)

# Different password
$hashedDiff = & $mcrypt hash "DifferentPass" $hashSalt
Test-Result "Different password gives different hash" ($hashed -ne $hashedDiff)

# ---------- 3. verify ----------
Write-Host "`n--- Test Group: verify ---"

$isValid = & $mcrypt verify $password $hashed
Test-Result "Correct password returns true" ($isValid -eq "true")

$isWrong = & $mcrypt verify "WrongPassword123!" $hashed
Test-Result "Wrong password returns false" ($isWrong -eq "false")

$isEmpty = & $mcrypt verify '""' $hashed
Test-Result "Empty-like password returns false" ($isEmpty -eq "false")

# Case sensitivity
$isCase = & $mcrypt verify "kopplgadag@2026" $hashed
Test-Result "Case-different password returns false" ($isCase -eq "false")

# ---------- 4. Edge Cases ----------
Write-Host "`n--- Test Group: Edge Cases ---"

# Special characters
$specialSalt = & $mcrypt gensalt 8
$specialHash = & $mcrypt hash '!@#%^&*()_+-=[]{}|;:,.<>?' $specialSalt
$specialVerify = & $mcrypt verify '!@#%^&*()_+-=[]{}|;:,.<>?' $specialHash
Test-Result "Special chars password works" ($specialVerify -eq "true")

# Low rounds
$lowSalt = & $mcrypt gensalt 4
$lowHash = & $mcrypt hash "QuickTest" $lowSalt
$lowVerify = & $mcrypt verify "QuickTest" $lowHash
Test-Result "Low rounds (4) works" ($lowVerify -eq "true")

# Invalid hash format
$badVerify = & $mcrypt verify "test" "not_a_valid_hash"
Test-Result "Invalid hash format returns false" ($badVerify -eq "false")

# ---------- 5. Error Handling ----------
Write-Host "`n--- Test Group: Error Handling ---"

# Missing args for hash
$hashErr = & $mcrypt hash "onlypassword" 2>&1
$hashErrStr = $hashErr | Out-String
Test-Result "hash with missing salt shows error" ($hashErrStr -match "Error|Missing")

# Unknown command
$unknownOut = & $mcrypt foobar 2>&1
$unknownStr = $unknownOut | Out-String
Test-Result "Unknown command shows help message" ($unknownStr -match "Unknown")

# ---------- Summary ----------
Write-Host ("`n" + "=" * 60)
$total = $PASS + $FAIL
Write-Host "  Results: $PASS/$total passed, $FAIL failed"
Write-Host ("=" * 60)

if ($FAIL -gt 0) {
    Write-Host "`n  Some tests failed!" -ForegroundColor Red
    exit 1
}
Write-Host "`n  All CLI tests passed!`n" -ForegroundColor Green
