/**
 * mcrypt Production Test - Node.js (npm)
 * ========================================
 * Install: npm install mcrypt-mass-crypto
 * Run:     node tests/test_production_nodejs.js
 *
 * This test simulates how end-users require and use mcrypt
 * after installing from npm via: npm install mcrypt-mass-crypto
 */

// ============================================================
// Production Import - exactly how users require mcrypt
// ============================================================
const mcrypt = require('mcrypt-mass-crypto');

console.log('='.repeat(60));
console.log('  mcrypt Production Test (Node.js / npm)');
console.log('  Package: mcrypt-mass-crypto');
console.log('='.repeat(60));

let PASS = 0;
let FAIL = 0;

function test(name, condition) {
    if (condition) {
        PASS++;
        console.log(`  [PASS] ${name}`);
    } else {
        FAIL++;
        console.log(`  [FAIL] ${name}`);
    }
}

// ---------- 1. User Registration Flow ----------
console.log('\n--- Scenario: User Registration ---');

const password = 'MySecureP@ssw0rd!2026';

// Step 1: Generate salt (server-side on sign-up)
const salt = mcrypt.gensalt(12, 16);
test('gensalt() returns valid salt', salt.startsWith('$mcrypt$v3$'));
console.log(`    Salt: ${salt}`);

// Step 2: Hash password before storing in DB
const storedHash = mcrypt.hashWithSalt(password, salt);
test('hashWithSalt() returns hash', storedHash.length > salt.length);
console.log(`    Hash: ${storedHash}`);

// ---------- 2. User Login Flow ----------
console.log('\n--- Scenario: User Login ---');

// Correct password login
const loginResult = mcrypt.verify(password, storedHash);
test('Login with correct password -> true', loginResult === true);

// Wrong password login attempt
const hackerResult = mcrypt.verify('hacker123', storedHash);
test('Login with wrong password -> false', hackerResult === false);

// Brute force attempt with similar password
const similarResult = mcrypt.verify('MySecureP@ssw0rd!2027', storedHash);
test('Login with similar password -> false', similarResult === false);

// Empty password attempt
const emptyResult = mcrypt.verify('', storedHash);
test('Login with empty password -> false', emptyResult === false);

// ---------- 3. Password Change Flow ----------
console.log('\n--- Scenario: Password Change ---');

const newPassword = 'NewStr0ngerP@ss!2026';
const newSalt = mcrypt.gensalt(12, 16);
const newHash = mcrypt.hashWithSalt(newPassword, newSalt);

// Old password should no longer work
const oldLogin = mcrypt.verify(password, newHash);
test('Old password fails against new hash -> false', oldLogin === false);

// New password should work
const newLogin = mcrypt.verify(newPassword, newHash);
test('New password works against new hash -> true', newLogin === true);

// ---------- 4. Batch Hashing (Mass Crypto) ----------
console.log('\n--- Scenario: Batch Hashing (5 users) ---');

const users = [
    { name: 'alice', password: 'Alice@Pass123' },
    { name: 'bob', password: 'B0bSecure!' },
    { name: 'charlie', password: 'Ch@rlie2026' },
    { name: 'diana', password: 'D1ana_Strong' },
    { name: 'eve', password: 'Ev3_P@ssword' },
];

const hashes = {};
for (const user of users) {
    const s = mcrypt.gensalt(10, 16);
    hashes[user.name] = mcrypt.hashWithSalt(user.password, s);
}

// Verify all users
const allValid = users.every(u => mcrypt.verify(u.password, hashes[u.name]));
test('All 5 users hash and verify correctly', allValid);

// Cross-verify: alice's password should NOT match bob's hash
const crossCheck = mcrypt.verify('Alice@Pass123', hashes['bob']);
test("Alice's password fails against Bob's hash", crossCheck === false);

// ---------- 5. API Completeness Check ----------
console.log('\n--- Scenario: API Completeness ---');

test('mcrypt.gensalt exists', typeof mcrypt.gensalt === 'function');
test('mcrypt.hashWithSalt exists', typeof mcrypt.hashWithSalt === 'function');
test('mcrypt.verify exists', typeof mcrypt.verify === 'function');

// ---------- Summary ----------
console.log('\n' + '='.repeat(60));
const total = PASS + FAIL;
console.log(`  Results: ${PASS}/${total} passed, ${FAIL} failed`);
console.log('='.repeat(60));

if (FAIL > 0) {
    console.log('\n  PRODUCTION TEST FAILED!');
    process.exit(1);
}

console.log('\n  All production tests passed!');
console.log('  mcrypt is ready for production use.\n');
