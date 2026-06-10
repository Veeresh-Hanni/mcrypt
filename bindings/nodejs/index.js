const koffi = require('koffi');
const path = require('path');

const nativeLibraryName = process.platform === 'win32'
    ? 'mcrypt_native.dll'
    : process.platform === 'darwin'
        ? 'libmcrypt_native.dylib'
        : 'libmcrypt_native.so';

// Try bundled binary first (production), then development build
const bundledPath = path.join(__dirname, 'lib', nativeLibraryName);
const devPath = path.join(__dirname, '../../target/release', nativeLibraryName);

let libPath;
try {
    require('fs').accessSync(bundledPath);
    libPath = bundledPath;
} catch {
    libPath = devPath; // fallback for development
}

const lib = koffi.load(libPath);

// koffi ಶೈಲಿಯಲ್ಲಿ ಫಂಕ್ಷನ್ ಸಿಗ್ನೇಚರ್ ಮ್ಯಾಪ್ ಮಾಡುವುದು
const mcrypt_gensalt = lib.func('mcrypt_gensalt', 'string', ['uint32', 'size_t']);
const mcrypt_hash_with_salt = lib.func('mcrypt_hash_with_salt', 'string', ['string', 'string']);
const mcrypt_verify = lib.func('mcrypt_verify', 'bool', ['string', 'string']);

module.exports = {
    gensalt: (rounds, len) => mcrypt_gensalt(rounds, len),
    hashWithSalt: (pass, salt) => mcrypt_hash_with_salt(pass, salt),
    verify: (pass, hash) => mcrypt_verify(pass, hash)
};
