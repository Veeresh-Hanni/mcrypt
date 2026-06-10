/**
 * mcrypt (Mass Crypto) - Node.js Binding Tests
 * ==============================================
 * Run: cd bindings/nodejs && npm install && cd ../.. && node tests/test_nodejs.js
 * 
 * Requires: mcrypt_native.dll/.so/.dylib in target/release/
 */

const path = require('path');
const mcrypt = require(path.join(__dirname, '..', 'bindings', 'nodejs', 'index.js'));

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

function main() {
    console.log('='.repeat(60));
    console.log('  mcrypt Node.js Binding Test Suite');
    console.log('='.repeat(60));

    // ---------- 1. gensalt() ----------
    console.log('\n--- Test Group: gensalt() ---');

    const salt = mcrypt.gensalt(12, 16);
    test('gensalt returns a string', typeof salt === 'string');
    test('gensalt starts with $mcrypt$', salt.startsWith('$mcrypt$'));
    test('gensalt contains version v3', salt.includes('$v3$'));
    test('gensalt contains round info r12', salt.includes('$r12$'));
    test('gensalt contains salt length sl16', salt.includes('$sl16$'));

    // Different rounds
    const salt4 = mcrypt.gensalt(4, 16);
    test('gensalt rounds=4 has $r04$', salt4.includes('$r04$'));

    const salt20 = mcrypt.gensalt(20, 16);
    test('gensalt rounds=20 has $r20$', salt20.includes('$r20$'));

    // Unique salts
    const saltA = mcrypt.gensalt(12, 16);
    const saltB = mcrypt.gensalt(12, 16);
    test('Two gensalt calls produce different salts', saltA !== saltB);

    // ---------- 2. hashWithSalt() ----------
    console.log('\n--- Test Group: hashWithSalt() ---');

    const password = 'KoppalGadag@2026';
    const hashSalt = mcrypt.gensalt(12, 16);
    const hashed = mcrypt.hashWithSalt(password, hashSalt);

    test('hashWithSalt returns a string', typeof hashed === 'string');
    test('hash starts with salt prefix', hashed.startsWith(hashSalt));
    test('hash is longer than salt', hashed.length > hashSalt.length);

    // Hash format validation
    const parts = hashed.split('$');
    test('hash has 7 parts when split by $', parts.length === 7);
    test('hash hex digest is 64 chars (SHA-256)', parts[6].length === 64);

    // Deterministic
    const hashedAgain = mcrypt.hashWithSalt(password, hashSalt);
    test('Same password+salt gives same hash', hashed === hashedAgain);

    // Different password
    const hashedDiff = mcrypt.hashWithSalt('DifferentPass', hashSalt);
    test('Different password gives different hash', hashed !== hashedDiff);

    // ---------- 3. verify() ----------
    console.log('\n--- Test Group: verify() ---');

    const isValid = mcrypt.verify(password, hashed);
    test('Correct password verifies as true', isValid === true);

    const isWrong = mcrypt.verify('WrongPassword123!', hashed);
    test('Wrong password verifies as false', isWrong === false);

    const isEmpty = mcrypt.verify('', hashed);
    test('Empty password verifies as false', isEmpty === false);

    // Case sensitivity
    const isCase = mcrypt.verify('kopplgadag@2026', hashed);
    test('Case-different password verifies as false', isCase === false);

    // ---------- 4. Edge Cases ----------
    console.log('\n--- Test Group: Edge Cases ---');

    // Long password
    const edgeSalt = mcrypt.gensalt(8, 16);
    const longPass = 'A'.repeat(1000);
    const longHash = mcrypt.hashWithSalt(longPass, edgeSalt);
    test('Long password (1000 chars) hashes successfully', typeof longHash === 'string');
    test('Long password verifies correctly', mcrypt.verify(longPass, longHash) === true);

    // Special characters
    const specialPass = '!@#$%^&*()_+-=[]{}|;:,.<>?';
    const specialHash = mcrypt.hashWithSalt(specialPass, edgeSalt);
    test('Special chars password hashes successfully', typeof specialHash === 'string');
    test('Special chars password verifies correctly', mcrypt.verify(specialPass, specialHash) === true);

    // Low rounds (faster)
    const lowSalt = mcrypt.gensalt(4, 16);
    const lowHash = mcrypt.hashWithSalt('QuickTest', lowSalt);
    test('Low rounds (4) hash works', mcrypt.verify('QuickTest', lowHash) === true);
    test('Low rounds wrong password fails', mcrypt.verify('WrongTest', lowHash) === false);

    // ---------- Summary ----------
    console.log('\n' + '='.repeat(60));
    const total = PASS + FAIL;
    console.log(`  Results: ${PASS}/${total} passed, ${FAIL} failed`);
    console.log('='.repeat(60));

    if (FAIL > 0) {
        process.exit(1);
    }
    console.log('\n  All Node.js tests passed!\n');
}

main();
